package com.gams.finanzas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.gams.finanzas.entity.Frecuencia;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Crear un movimiento automático.
 * "fecha" es desde cuándo empieza: el primer registro es la primera fecha del calendario a partir de ese día
 * (si ya pasó, se registra al crear). Si no se indica la frecuencia, es mensual; si no se indica el día del
 * mes, se usa el de "fecha". El día 31 significa "el último día de cada mes".
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

        @NotNull(message = "Indica desde qué fecha empieza")
        @PastOrPresent(message = "La fecha no puede ser futura: elige el día del mes para programar uno futuro")
        LocalDate fecha,

        Frecuencia frecuencia,

        @Min(value = 1, message = "El día del mes debe estar entre 1 y 31")
        @Max(value = 31, message = "El día del mes debe estar entre 1 y 31")
        Integer dia) {
}
