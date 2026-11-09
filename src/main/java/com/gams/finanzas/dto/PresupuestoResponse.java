package com.gams.finanzas.dto;

import java.math.BigDecimal;

import com.gams.finanzas.entity.PresupuestoMensual;

public record PresupuestoResponse(int anio, int mes, BigDecimal limiteGasto, BigDecimal ahorroObjetivo) {

    public static PresupuestoResponse desde(PresupuestoMensual presupuesto) {
        return new PresupuestoResponse(presupuesto.getAnio(), presupuesto.getMes(), presupuesto.getLimiteGasto(),
                presupuesto.getAhorroObjetivo());
    }
}
