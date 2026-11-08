package com.gams.finanzas.dto;

import java.time.Instant;

/** Respuesta de registro y login: el token de sesión, cuándo vence y quién es el usuario. */
public record AuthResponse(String token, Instant expiraEn, UsuarioResponse usuario) {
}
