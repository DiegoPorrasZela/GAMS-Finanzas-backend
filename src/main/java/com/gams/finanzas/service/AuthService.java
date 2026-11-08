package com.gams.finanzas.service;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gams.finanzas.dto.AuthResponse;
import com.gams.finanzas.dto.LoginRequest;
import com.gams.finanzas.dto.RegistroRequest;
import com.gams.finanzas.dto.UsuarioResponse;
import com.gams.finanzas.entity.Rol;
import com.gams.finanzas.entity.Usuario;
import com.gams.finanzas.exception.ConflictoException;
import com.gams.finanzas.exception.CredencialesInvalidasException;
import com.gams.finanzas.repository.RolRepository;
import com.gams.finanzas.repository.UsuarioRepository;

/** Registro e inicio de sesión. Las cuentas nuevas siempre tienen el rol USUARIO. */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse registrar(RegistroRequest datos) {
        String email = normalizar(datos.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new ConflictoException("Ya existe una cuenta con el correo " + email);
        }

        Rol rolUsuario = rolRepository.findByNombre(Rol.USUARIO).orElseThrow();
        Usuario usuario = usuarioRepository.save(
                new Usuario(datos.nombre().trim(), email, passwordEncoder.encode(datos.password()), rolUsuario));
        return iniciarSesion(usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest datos) {
        Usuario usuario = usuarioRepository.findByEmail(normalizar(datos.email()))
                .filter(encontrado -> passwordEncoder.matches(datos.password(), encontrado.getPassword()))
                .orElseThrow(CredencialesInvalidasException::new);
        return iniciarSesion(usuario);
    }

    private AuthResponse iniciarSesion(Usuario usuario) {
        Jwt token = jwtService.generar(usuario);
        return new AuthResponse(token.getTokenValue(), token.getExpiresAt(), UsuarioResponse.desde(usuario));
    }

    private static String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
