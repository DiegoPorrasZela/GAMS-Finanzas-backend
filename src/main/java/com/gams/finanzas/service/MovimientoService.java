package com.gams.finanzas.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gams.finanzas.dto.MovimientoRequest;
import com.gams.finanzas.dto.MovimientoResponse;
import com.gams.finanzas.entity.Categoria;
import com.gams.finanzas.entity.Movimiento;
import com.gams.finanzas.entity.Usuario;
import com.gams.finanzas.exception.ConflictoException;
import com.gams.finanzas.exception.DatosInvalidosException;
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
    private final AhorroService ahorroService;

    public MovimientoService(MovimientoRepository movimientoRepository, UsuarioRepository usuarioRepository,
            CategoriaService categoriaService, RecurrenteService recurrenteService, AhorroService ahorroService) {
        this.movimientoRepository = movimientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaService = categoriaService;
        this.recurrenteService = recurrenteService;
        this.ahorroService = ahorroService;
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
        // No se puede retirar de los ahorros más de lo que hay libre en ellos
        if (categoria.esRetiroAhorros()) {
            BigDecimal libres = ahorroService.libres(usuarioId);
            if (datos.monto().compareTo(libres) > 0) {
                throw new ConflictoException("Solo tienes " + AhorroService.soles(libres)
                        + " libres en tus ahorros para retirar (lo que está en tus metas no se puede retirar).");
            }
        }

        validarMonedaOriginal(datos, categoria);

        Movimiento movimiento = new Movimiento(usuario, datos.descripcion().trim(), datos.monto(), datos.fecha(),
                categoria);
        movimiento.cambiarMonedaOriginal(datos.monedaOriginal(), datos.montoOriginal());
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

        validarMonedaOriginal(datos, categoria);

        boolean afectaAhorros = mueveAhorros(movimiento.getCategoria()) || mueveAhorros(categoria);
        movimiento.actualizar(datos.descripcion().trim(), datos.monto(), datos.fecha(), categoria);
        movimiento.cambiarMonedaOriginal(datos.monedaOriginal(), datos.montoOriginal());
        if (afectaAhorros) {
            movimientoRepository.flush();
            ahorroService.validarNoNegativos(usuarioId); // si falla, la transacción deshace el cambio
        }
        return MovimientoResponse.desde(movimiento);
    }

    @Transactional
    public void eliminar(Long id, Long usuarioId) {
        Movimiento movimiento = buscarPropio(id, usuarioId);
        movimientoRepository.delete(movimiento);
        // Borrar un ahorro cuyo dinero ya está en una meta dejaría los ahorros en negativo
        if (movimiento.getCategoria().esAhorro()) {
            movimientoRepository.flush();
            ahorroService.validarNoNegativos(usuarioId);
        }
    }

    /**
     * Un pago en dólares o euros trae la moneda y el monto original juntos.
     * El saldo inicial y los ahorros se manejan siempre en soles.
     */
    private static void validarMonedaOriginal(MovimientoRequest datos, Categoria categoria) {
        if ((datos.monedaOriginal() == null) != (datos.montoOriginal() == null)) {
            throw new DatosInvalidosException("Indica la moneda y el monto original juntos (o ninguno si fue en soles)");
        }
        if (datos.monedaOriginal() != null && categoria.esDelSistema()) {
            throw new DatosInvalidosException("\"" + categoria.getNombre() + "\" se registra siempre en soles");
        }
    }

    private static boolean mueveAhorros(Categoria categoria) {
        return categoria.esAhorro() || categoria.esRetiroAhorros();
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
