package com.gams.finanzas.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.gams.finanzas.repository.UsuarioRepository;
import com.gams.finanzas.service.JwtService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

/** Firma (encoder) y verificación (decoder) de los tokens JWT con una clave secreta HMAC-SHA256. */
@Configuration
public class JwtConfig {

    @Bean
    SecretKey claveJwt(@Value("${app.jwt.secreto}") String secreto) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRETO debe tener al menos 32 caracteres");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey claveJwt) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(claveJwt));
    }

    /**
     * Rechaza tokens con firma inválida, vencidos, emitidos por otra aplicación o de una cuenta que ya
     * fue eliminada (sin esto, el token de un usuario eliminado seguiría sirviendo hasta vencer).
     */
    @Bean
    JwtDecoder jwtDecoder(SecretKey claveJwt, UsuarioRepository usuarioRepository) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(claveJwt).macAlgorithm(MacAlgorithm.HS256).build();
        OAuth2TokenValidator<Jwt> cuentaExiste = token -> usuarioRepository.existsById(JwtService.idUsuario(token))
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "La cuenta de este token ya no existe", null));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(JwtService.EMISOR), cuentaExiste));
        return decoder;
    }
}
