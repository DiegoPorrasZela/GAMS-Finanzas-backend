package com.gams.finanzas.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gams.finanzas.dto.MovimientoRequest;
import com.gams.finanzas.dto.MovimientoResponse;
import com.gams.finanzas.entity.Categoria;
import com.gams.finanzas.entity.Movimiento;
import com.gams.finanzas.entity.Usuario;
import com.gams.finanzas.exception.ConflictoException;
import com.gams.finanzas.exception.RecursoNoEncontradoException;
import com.gams.finanzas.repository.MovimientoRepository;
import com.gams.finanzas.repository.UsuarioRepository;

/**
 * CRUD de movimientos. Todas las operaciones reciben el id del usuario dueño:
 * un usuario solo puede ver, editar o eliminar sus propios movimientos.
 */
@Service
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaService categoriaService;
    private final RecurrenteService recurrenteService;

    public MovimientoService(MovimientoRepository movimientoRepository, UsuarioRepository usuarioRepository,
            CategoriaService categoriaService, RecurrenteService recurrenteService) {
        this.movimientoRepository = movimientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaService = categoriaService;
        this.recurrenteService = recurrenteService;
    }

    /** Antes de listar, registra los movimientos automáticos (sueldo, alquiler...) cuya fecha ya llegó. */
    @Transactional
    public List<MovimientoResponse> listar(Long usuarioId) {
        recurrenteService.generarPendientes(usuarioId);
        return movimientoRepository.findByUsuarioIdOrderByFechaDescIdDesc(usuarioId).stream()
                .map(MovimientoResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public MovimientoResponse obtener(Long id, Long usuarioId) {
        return MovimientoResponse.desde(buscarPropio(id, usuarioId));
    }

    @Transactional
    public MovimientoResponse crear(MovimientoRequest datos, Long usuarioId) {
        Usuario usuario = usuarioRepository.getReferenceById(usuarioId);
        Categoria categoria = categoriaService.buscarEntidad(datos.categoriaId());
        if (categoria.esSaldoInicial() && movimientoRepository.existsByUsuarioIdAndCategoriaId(usuarioId, categoria.getId())) {
            throw saldoInicialRepetido();
        }

        Movimiento movimiento = new Movimiento(usuario, datos.descripcion().trim(), datos.monto(), datos.fecha(),
                categoria);
        return MovimientoResponse.desde(movimientoRepository.save(movimiento));
    }

    @Transactional
    public MovimientoResponse actualizar(Long id, MovimientoRequest datos, Long usuarioId) {
        Movimiento movimiento = buscarPropio(id, usuarioId);
        Categoria categoria = categoriaService.buscarEntidad(datos.categoriaId());
        if (categoria.esSaldoInicial()
                && movimientoRepository.existsByUsuarioIdAndCategoriaIdAndIdNot(usuarioId, categoria.getId(), id)) {
            throw saldoInicialRepetido();
        }

        movimiento.actualizar(datos.descripcion().trim(), datos.monto(), datos.fecha(), categoria);
        return MovimientoResponse.desde(movimiento);
    }

    @Transactional
    public void eliminar(Long id, Long usuarioId) {
        movimientoRepository.delete(buscarPropio(id, usuarioId));
    }

    /** El saldo inicial es el dinero con el que se empieza: se registra una sola vez (si cambia, se edita). */
    private static ConflictoException saldoInicialRepetido() {
        return new ConflictoException("Ya registraste tu saldo inicial. Si cambió, edita el que ya tienes.");
    }

    /** Si el movimiento es de otro usuario responde 404, igual que si no existiera (no revela que existe). */
    private Movimiento buscarPropio(Long id, Long usuarioId) {
        return movimientoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El movimiento " + id + " no existe"));
    }
}
