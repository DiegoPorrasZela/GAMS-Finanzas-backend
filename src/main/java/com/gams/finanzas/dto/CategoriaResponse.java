package com.gams.finanzas.dto;

import com.gams.finanzas.entity.Categoria;
import com.gams.finanzas.entity.TipoMovimiento;

/** Categoría tal como la recibe el cliente. */
public record CategoriaResponse(Long id, String nombre, TipoMovimiento tipo, String icono, String color) {

    public static CategoriaResponse desde(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNombre(), categoria.getTipo(),
                categoria.getIcono(), categoria.getColor());
    }
}
