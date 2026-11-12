package com.gams.finanzas.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
import com.gams.finanzas.exception.DatosInvalidosException;
import com.gams.finanzas.repository.RolRepository;
import com.gams.finanzas.repository.UsuarioRepository;

/**
 * Registro e inicio de sesión. Las cuentas nuevas siempre tienen el rol USUARIO.
 * Los intentos se limitan por dirección IP para que nadie pruebe contraseñas ni cree cuentas sin parar.
 */
@Service
public class AuthService {

    private static final int MAXIMO_BYTES_PASSWORD = 72;

    // Contraseñas equivocadas seguidas para un mismo correo desde una misma IP
    private static final int MAXIMO_LOGINS_FALLIDOS = 5;
    private static final Duration ESPERA_LOGIN = Duration.ofMinutes(15);
    // Cuentas nuevas desde una misma IP (holgado: en la universidad muchos comparten la misma IP)
    private static final int MAXIMO_REGISTROS = 20;
    private static final Duration ESPERA_REGISTRO = Duration.ofHours(1);

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LimiteIntentos limiteIntentos;

    public AuthService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService, LimiteIntentos limiteIntentos) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.limiteIntentos = limiteIntentos;
    }

    @Transactional
    public AuthResponse registrar(RegistroRequest datos, String ip) {
        String claveIntentos = "registro:" + ip;
        limiteIntentos.verificar(claveIntentos, MAXIMO_REGISTROS);

        String email = normalizar(datos.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new ConflictoException("Ya existe una cuenta con el correo " + email);
        }
        // BCrypt acepta hasta 72 bytes, no 72 caracteres: una tilde o una ñ ocupan 2 bytes
        if (datos.password().getBytes(StandardCharsets.UTF_8).length > MAXIMO_BYTES_PASSWORD) {
            throw new DatosInvalidosException(
                    "La contraseña es demasiado larga: usa una más corta (las tildes y la ñ cuentan doble)");
        }

        Rol rolUsuario = rolRepository.findByNombre(Rol.USUARIO).orElseThrow();
        Usuario usuario = usuarioRepository.save(
                new Usuario(datos.nombre().trim(), email, passwordEncoder.encode(datos.password()), rolUsuario));
        limiteIntentos.registrar(claveIntentos, ESPERA_REGISTRO);
        return iniciarSesion(usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest datos, String ip) {
        String email = normalizar(datos.email());
        // Por IP y correo: quien falla no bloquea esa cuenta a los demás (ej. la cuenta demo)
        String claveIntentos = "login:" + ip + ":" + email;
        limiteIntentos.verificar(claveIntentos, MAXIMO_LOGINS_FALLIDOS);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .filter(encontrado -> passwordEncoder.matches(datos.password(), encontrado.getPassword()))
                .orElseThrow(() -> {
                    limiteIntentos.registrar(claveIntentos, ESPERA_LOGIN);
                    return new CredencialesInvalidasException();
                });
        limiteIntentos.olvidar(claveIntentos);
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
