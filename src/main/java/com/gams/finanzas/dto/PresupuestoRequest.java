package com.gams.finanzas.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/** Plan del mes que define el usuario. El año y el mes van en la URL. */
public record PresupuestoRequest(

        @NotNull(message = "El límite de gasto es obligatorio")
        @Positive(message = "El límite de gasto debe ser mayor a 0")
        @Digits(integer = 10, fraction = 2, message = "El límite admite como máximo 2 decimales")
        BigDecimal limiteGasto,

        @NotNull(message = "La meta de ahorro es obligatoria (puede ser 0)")
        @PositiveOrZero(message = "La meta de ahorro no puede ser negativa")
        @Digits(integer = 10, fraction = 2, message = "La meta de ahorro admite como máximo 2 decimales")
        BigDecimal ahorroObjetivo) {
}
