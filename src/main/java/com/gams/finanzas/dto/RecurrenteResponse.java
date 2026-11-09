package com.gams.finanzas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.gams.finanzas.entity.MovimientoRecurrente;
import com.gams.finanzas.entity.TipoMovimiento;

public record RecurrenteResponse(
        Long id,
        String descripcion,
        BigDecimal monto,
        TipoMovimiento tipo,
        Long categoriaId,
        String categoria,
        int dia,
        LocalDate proximoCargo,
        boolean activo) {

    public static RecurrenteResponse desde(MovimientoRecurrente recurrente) {
        return new RecurrenteResponse(
                recurrente.getId(),
                recurrente.getDescripcion(),
                recurrente.getMonto(),
                recurrente.getCategoria().getTipo(),
                recurrente.getCategoria().getId(),
                recurrente.getCategoria().getNombre(),
                recurrente.getDia(),
                recurrente.getProximoCargo(),
                recurrente.isActivo());
    }
}
