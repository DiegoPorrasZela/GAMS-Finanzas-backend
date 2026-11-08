package com.gams.finanzas.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.gams.finanzas.entity.Usuario;

/** Genera los tokens JWT que identifican la sesión de un usuario. */
@Service
public class JwtService {

    public static final String EMISOR = "gams-finanzas";

    private final JwtEncoder jwtEncoder;
    private final long horasDeSesion;

    public JwtService(JwtEncoder jwtEncoder, @Value("${app.jwt.horas}") long horasDeSesion) {
        this.jwtEncoder = jwtEncoder;
        this.horasDeSesion = horasDeSesion;
    }

    public Jwt generar(Usuario usuario) {
        Instant ahora = Instant.now();

        // Patrón Builder: el token se arma dato por dato
        JwtClaimsSet datos = JwtClaimsSet.builder()
                .issuer(EMISOR)
                .issuedAt(ahora)
                .expiresAt(ahora.plus(horasDeSesion, ChronoUnit.HOURS))
                .subject(String.valueOf(usuario.getId()))
                .claim("nombre", usuario.getNombre())
                .claim("email", usuario.getEmail())
                .claim("rol", usuario.getRol().getNombre())
                .build();

        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(cabecera, datos));
    }

    /** Id del usuario dueño del token (va en el campo "sub"). */
    public static Long idUsuario(Jwt token) {
        return Long.valueOf(token.getSubject());
    }
}
