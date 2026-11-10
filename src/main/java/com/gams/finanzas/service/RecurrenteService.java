package com.gams.finanzas.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gams.finanzas.dto.RecurrenteCambioRequest;
import com.gams.finanzas.dto.RecurrenteRequest;
import com.gams.finanzas.dto.RecurrenteResponse;
import com.gams.finanzas.entity.Categoria;
import com.gams.finanzas.entity.Frecuencia;
import com.gams.finanzas.entity.MovimientoRecurrente;
import com.gams.finanzas.exception.DatosInvalidosException;
import com.gams.finanzas.exception.RecursoNoEncontradoException;
import com.gams.finanzas.repository.MovimientoRecurrenteRepository;
import com.gams.finanzas.repository.MovimientoRepository;
import com.gams.finanzas.repository.UsuarioRepository;

/**
 * Ingresos y gastos automáticos (se repiten cada mes).
 * No hay una tarea programada: el servidor gratuito se "duerme" y no la ejecutaría. En su lugar, cada vez
 * que el usuario consulta sus movimientos se registran los que ya llegaron a su fecha.
 */
@Service
public class RecurrenteService {

    private final MovimientoRecurrenteRepository recurrenteRepository;
    private final MovimientoRepository movimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaService categoriaService;

    public RecurrenteService(MovimientoRecurrenteRepository recurrenteRepository,
            MovimientoRepository movimientoRepository, UsuarioRepository usuarioRepository,
            CategoriaService categoriaService) {
        this.recurrenteRepository = recurrenteRepository;
        this.movimientoRepository = movimientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaService = categoriaService;
    }

    @Transactional(readOnly = true)
    public List<RecurrenteResponse> listar(Long usuarioId) {
        return recurrenteRepository.findByUsuarioIdOrderByIdAsc(usuarioId).stream()
                .map(RecurrenteResponse::desde)
                .toList();
    }

    /** Crea la repetición y registra de una vez las fechas que ya llegaron (ej. si la primera es hoy). */
    @Transactional
    public RecurrenteResponse crear(RecurrenteRequest datos, Long usuarioId) {
        LocalDate hoy = LocalDate.now();
        if (datos.fecha().isBefore(hoy.minusYears(1))) {
            throw new DatosInvalidosException("El primer registro no puede ser de hace más de un año");
        }
        Categoria categoria = categoriaValida(datos.categoriaId());
        Frecuencia frecuencia = datos.frecuencia() != null ? datos.frecuencia() : Frecuencia.MENSUAL;
        MovimientoRecurrente recurrente = recurrenteRepository.save(new MovimientoRecurrente(
                usuarioRepository.getReferenceById(usuarioId), categoria,
                datos.descripcion().trim(), datos.monto(), datos.fecha(), frecuencia, datos.dia()));
        movimientoRepository.saveAll(recurrente.generarHasta(hoy));
        return RecurrenteResponse.desde(recurrente);
    }

    @Transactional
    public RecurrenteResponse actualizar(Long id, RecurrenteCambioRequest datos, Long usuarioId) {
        MovimientoRecurrente recurrente = buscarPropio(id, usuarioId);
        recurrente.actualizar(datos.descripcion().trim(), datos.monto(), categoriaValida(datos.categoriaId()));
        if (datos.activo() && !recurrente.isActivo()) {
            recurrente.reanudar(LocalDate.now());
        } else if (!datos.activo() && recurrente.isActivo()) {
            recurrente.pausar();
        }
        return RecurrenteResponse.desde(recurrente);
    }

    /** Los movimientos que ya generó se conservan (la base de datos les quita el vínculo). */
    @Transactional
    public void eliminar(Long id, Long usuarioId) {
        recurrenteRepository.delete(buscarPropio(id, usuarioId));
    }

    /** Registra los movimientos automáticos cuya fecha ya llegó. */
    @Transactional
    public void generarPendientes(Long usuarioId) {
        LocalDate hoy = LocalDate.now();
        for (MovimientoRecurrente recurrente : recurrenteRepository.buscarPendientes(usuarioId, hoy)) {
            movimientoRepository.saveAll(recurrente.generarHasta(hoy));
        }
    }

    private Categoria categoriaValida(Long categoriaId) {
        Categoria categoria = categoriaService.buscarEntidad(categoriaId);
        if (categoria.esSaldoInicial()) {
            throw new DatosInvalidosException("El saldo inicial se registra una sola vez: no puede repetirse");
        }
        if (categoria.esRetiroAhorros()) {
            throw new DatosInvalidosException("Los retiros de ahorros no pueden ser automáticos");
        }
        return categoria;
    }

    private MovimientoRecurrente buscarPropio(Long id, Long usuarioId) {
        return recurrenteRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El movimiento automático " + id + " no existe"));
    }
}
