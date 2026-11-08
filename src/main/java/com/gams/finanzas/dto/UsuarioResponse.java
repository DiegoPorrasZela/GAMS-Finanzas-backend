package com.gams.finanzas.dto;

import com.gams.finanzas.entity.Usuario;

/** Datos públicos de un usuario (sin la contraseña). */
public record UsuarioResponse(Long id, String nombre, String email, String rol) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getEmail(),
                usuario.getRol().getNombre());
    }
}
