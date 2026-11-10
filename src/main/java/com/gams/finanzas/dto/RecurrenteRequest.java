package com.gams.finanzas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.gams.finanzas.entity.Frecuencia;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Crear un movimiento automático. La fecha del primer registro define el día en que se repite
 * (del mes o de la semana). Si no se indica la frecuencia, es mensual.
 */
public record RecurrenteRequest(

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 60, message = "La descripción no puede tener más de 60 caracteres")
        String descripcion,

        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor a 0")
        @Digits(integer = 10, fraction = 2, message = "El monto admite como máximo 2 decimales")
        BigDecimal monto,

        @NotNull(message = "La categoría es obligatoria")
        Long categoriaId,

        @NotNull(message = "La fecha del primer registro es obligatoria")
        @PastOrPresent(message = "La fecha no puede ser futura")
        LocalDate fecha,

        Frecuencia frecuencia) {
}
