package com.gams.finanzas.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Editar un movimiento automático, o pausarlo y reanudarlo con "activo". */
public record RecurrenteCambioRequest(

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 60, message = "La descripción no puede tener más de 60 caracteres")
        String descripcion,

        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor a 0")
        @Digits(integer = 10, fraction = 2, message = "El monto admite como máximo 2 decimales")
        BigDecimal monto,

        @NotNull(message = "La categoría es obligatoria")
        Long categoriaId,

        @NotNull(message = "Indica si está activo")
        Boolean activo) {
}
