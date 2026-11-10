package com.gams.finanzas.service;

import java.math.BigDecimal;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gams.finanzas.entity.Categoria;
import com.gams.finanzas.entity.TipoMovimiento;
import com.gams.finanzas.exception.ConflictoException;
import com.gams.finanzas.repository.MetaAhorroRepository;
import com.gams.finanzas.repository.MovimientoRepository;

/**
 * Ahorros del usuario: una "cajita" separada de su saldo.
 * Saldo ──(Ahorro)──► Ahorros ──(aportes)──► Metas;  Ahorros ──(Retiro de ahorros)──► Saldo.
 * Lo que hay en ahorros no se guarda en ninguna tabla: se calcula con los movimientos y los aportes.
 */
@Service
public class AhorroService {

    private final MovimientoRepository movimientoRepository;
    private final MetaAhorroRepository metaRepository;

    public AhorroService(MovimientoRepository movimientoRepository, MetaAhorroRepository metaRepository) {
        this.movimientoRepository = movimientoRepository;
        this.metaRepository = metaRepository;
    }

    /** Dinero en ahorros que todavía no está en ninguna meta: separado − retirado − aportado a metas. */
    @Transactional(readOnly = true)
    public BigDecimal libres(Long usuarioId) {
        BigDecimal separado = movimientoRepository.sumarPorCategoria(usuarioId, Categoria.AHORRO,
                TipoMovimiento.GASTO);
        BigDecimal retirado = movimientoRepository.sumarPorCategoria(usuarioId, Categoria.RETIRO_AHORROS,
                TipoMovimiento.INGRESO);
        return separado.subtract(retirado).subtract(metaRepository.totalAportado(usuarioId));
    }

    /** Regla: los ahorros nunca pueden quedar en negativo (se revisa después de cambiar un ahorro o retiro). */
    public void validarNoNegativos(Long usuarioId) {
        BigDecimal libres = libres(usuarioId);
        if (libres.signum() < 0) {
            throw new ConflictoException("No se puede: tus ahorros quedarían en " + soles(libres)
                    + " porque ese dinero ya está en tus metas. Quita primero aportes de tus metas.");
        }
    }

    public static String soles(BigDecimal monto) {
        return String.format(Locale.ROOT, "S/ %.2f", monto);
    }
}
