package com.gams.finanzas.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.gams.finanzas.entity.Rol;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Autenticación y autorización.
 * Cada petición pasa por una cadena de filtros (patrón Chain of Responsibility): CORS → lectura del token
 * JWT del header "Authorization: Bearer ..." → reglas de acceso por ruta y por rol.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain cadenaDeFiltros(HttpSecurity http) throws Exception {
        http
                // API REST sin cookies de sesión: no aplica CSRF y cada petición trae su token
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reglas -> reglas
                        // Públicas
                        .requestMatchers(HttpMethod.POST, "/api/auth/registro", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/estado").permitAll()
                        // Cualquier usuario con sesión puede ver las categorías...
                        .requestMatchers(HttpMethod.GET, "/api/categorias").authenticated()
                        // ...pero solo el administrador las crea, edita o elimina, y gestiona usuarios
                        .requestMatchers("/api/categorias/**", "/api/usuarios/**").hasRole(Rol.ADMIN)
                        // Todo lo demás (movimientos, perfil) requiere sesión
                        .anyRequest().authenticated())
                .oauth2ResourceServer(jwt -> jwt
                        .jwt(config -> config.jwtAuthenticationConverter(rolesDesdeElToken()))
                        .authenticationEntryPoint(noAutenticado())
                        .accessDeniedHandler(sinPermiso()))
                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint(noAutenticado())
                        .accessDeniedHandler(sinPermiso()));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Permite que el frontend (otro dominio) llame al API desde el navegador. */
    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origenes}") String[] origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(origenes));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", config);
        return fuente;
    }

    /** Convierte el claim "rol" del token (ej. "ADMIN") en el permiso "ROLE_ADMIN" que usa hasRole(). */
    private JwtAuthenticationConverter rolesDesdeElToken() {
        JwtGrantedAuthoritiesConverter permisos = new JwtGrantedAuthoritiesConverter();
        permisos.setAuthoritiesClaimName("rol");
        permisos.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(permisos);
        return convertidor;
    }

    /** 401: no envió token, o es inválido o está vencido. */
    private AuthenticationEntryPoint noAutenticado() {
        return (peticion, respuesta, ex) -> {
            respuesta.setHeader("WWW-Authenticate", "Bearer");
            escribirError(respuesta, HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión: el token no existe, no es válido o ya venció");
        };
    }

    /** 403: tiene sesión, pero su rol no le permite esta acción. */
    private AccessDeniedHandler sinPermiso() {
        return (peticion, respuesta, ex) -> escribirError(respuesta, HttpStatus.FORBIDDEN,
                "No tienes permiso para realizar esta acción");
    }

    /** Mismo formato ProblemDetail que usa ManejadorGlobalErrores. */
    private static void escribirError(HttpServletResponse respuesta, HttpStatus estado, String detalle)
            throws IOException {
        respuesta.setStatus(estado.value());
        respuesta.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        respuesta.setCharacterEncoding("UTF-8");
        respuesta.getWriter().write("{\"status\":%d,\"title\":\"%s\",\"detail\":\"%s\"}"
                .formatted(estado.value(), estado.getReasonPhrase(), detalle));
    }
}
