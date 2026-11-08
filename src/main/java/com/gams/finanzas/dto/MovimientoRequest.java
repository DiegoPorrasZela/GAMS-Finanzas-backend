package com.gams.finanzas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Datos que envía el cliente para crear o editar un movimiento. El tipo sale de la categoría. */
public record MovimientoRequest(

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 60, message = "La descripción no puede tener más de 60 caracteres")
        String descripcion,

        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor a 0")
        @Digits(integer = 10, fraction = 2, message = "El monto admite como máximo 2 decimales")
        BigDecimal monto,

        @NotNull(message = "La fecha es obligatoria")
        LocalDate fecha,

        @NotNull(message = "La categoría es obligatoria")
        Long categoriaId) {
}
