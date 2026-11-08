package com.gams.finanzas.dto;

import com.gams.finanzas.entity.TipoMovimiento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos que envía el cliente para crear o editar una categoría. */
public record CategoriaRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50, message = "El nombre no puede tener más de 50 caracteres")
        String nombre,

        @NotNull(message = "El tipo es obligatorio (INGRESO o GASTO)")
        TipoMovimiento tipo) {
}
