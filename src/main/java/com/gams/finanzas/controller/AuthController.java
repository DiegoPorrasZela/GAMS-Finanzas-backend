package com.gams.finanzas.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.gams.finanzas.dto.AuthResponse;
import com.gams.finanzas.dto.LoginRequest;
import com.gams.finanzas.dto.RegistroRequest;
import com.gams.finanzas.dto.UsuarioResponse;
import com.gams.finanzas.service.AuthService;
import com.gams.finanzas.service.JwtService;
import com.gams.finanzas.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    public AuthController(AuthService authService, UsuarioService usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registrar(@Valid @RequestBody RegistroRequest datos) {
        return authService.registrar(datos);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest datos) {
        return authService.login(datos);
    }

    /** Datos del usuario de la sesión actual (sirve para comprobar que el token sigue siendo válido). */
    @GetMapping("/yo")
    public UsuarioResponse yo(@AuthenticationPrincipal Jwt token) {
        return usuarioService.obtener(JwtService.idUsuario(token));
    }
}
