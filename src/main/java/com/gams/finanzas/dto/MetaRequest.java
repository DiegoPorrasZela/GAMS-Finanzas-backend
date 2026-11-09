package com.gams.finanzas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Datos para crear o editar una meta de ahorro. La fecha límite es opcional. */
public record MetaRequest(

        @NotBlank(message = "El nombre de la meta es obligatorio")
        @Size(max = 60, message = "El nombre no puede tener más de 60 caracteres")
        String nombre,

        @NotNull(message = "El monto objetivo es obligatorio")
        @Positive(message = "El monto objetivo debe ser mayor a 0")
        @Digits(integer = 10, fraction = 2, message = "El monto admite como máximo 2 decimales")
        BigDecimal montoObjetivo,

        @FutureOrPresent(message = "La fecha límite no puede estar en el pasado")
        LocalDate fechaLimite) {
}
