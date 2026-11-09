package com.gams.finanzas.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import com.gams.finanzas.entity.Aporte;
import com.gams.finanzas.entity.MetaAhorro;

/** Meta con su avance calculado: cuánto se ahorró, cuánto falta y el porcentaje. */
public record MetaResponse(
        Long id,
        String nombre,
        BigDecimal montoObjetivo,
        LocalDate fechaLimite,
        BigDecimal ahorrado,
        BigDecimal faltante,
        int porcentaje,
        List<AporteResponse> aportes) {

    public record AporteResponse(Long id, BigDecimal monto, LocalDate fecha) {
    }

    public static MetaResponse desde(MetaAhorro meta) {
        BigDecimal ahorrado = meta.getAhorrado();
        BigDecimal faltante = meta.getMontoObjetivo().subtract(ahorrado).max(BigDecimal.ZERO);
        int porcentaje = ahorrado.multiply(BigDecimal.valueOf(100))
                .divide(meta.getMontoObjetivo(), 0, RoundingMode.DOWN)
                .intValue();

        List<AporteResponse> aportes = meta.getAportes().stream()
                .sorted(Comparator.comparing(Aporte::getFecha).thenComparing(Aporte::getId).reversed())
                .map(aporte -> new AporteResponse(aporte.getId(), aporte.getMonto(), aporte.getFecha()))
                .toList();

        return new MetaResponse(meta.getId(), meta.getNombre(), meta.getMontoObjetivo(), meta.getFechaLimite(),
                ahorrado, faltante, porcentaje, aportes);
    }
}
