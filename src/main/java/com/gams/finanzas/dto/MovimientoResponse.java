package com.gams.finanzas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.gams.finanzas.entity.Categoria;
import com.gams.finanzas.entity.Movimiento;
import com.gams.finanzas.entity.TipoMovimiento;

/** Movimiento tal como lo recibe el cliente (nunca se expone la entidad ni el usuario dueño). */
public record MovimientoResponse(
        Long id,
        String descripcion,
        BigDecimal monto,
        LocalDate fecha,
        TipoMovimiento tipo,
        Long categoriaId,
        String categoria,
        boolean automatico) {

    public static MovimientoResponse desde(Movimiento movimiento) {
        Categoria categoria = movimiento.getCategoria();
        return new MovimientoResponse(
                movimiento.getId(),
                movimiento.getDescripcion(),
                movimiento.getMonto(),
                movimiento.getFecha(),
                categoria.getTipo(),
                categoria.getId(),
                categoria.getNombre(),
                movimiento.esAutomatico());
    }
}
