package com.gams.finanzas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.gams.finanzas.dto.AporteRequest;
import com.gams.finanzas.dto.MetaRequest;
import com.gams.finanzas.dto.MetaResponse;
import com.gams.finanzas.service.JwtService;
import com.gams.finanzas.service.MetaAhorroService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/metas")
public class MetaAhorroController {

    private final MetaAhorroService metaService;

    public MetaAhorroController(MetaAhorroService metaService) {
        this.metaService = metaService;
    }

    @GetMapping
    public List<MetaResponse> listar(@AuthenticationPrincipal Jwt token) {
        return metaService.listar(JwtService.idUsuario(token));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MetaResponse crear(@Valid @RequestBody MetaRequest datos, @AuthenticationPrincipal Jwt token) {
        return metaService.crear(datos, JwtService.idUsuario(token));
    }

    @PutMapping("/{id}")
    public MetaResponse actualizar(@PathVariable Long id, @Valid @RequestBody MetaRequest datos,
            @AuthenticationPrincipal Jwt token) {
        return metaService.actualizar(id, datos, JwtService.idUsuario(token));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt token) {
        metaService.eliminar(id, JwtService.idUsuario(token));
    }

    /** Registra un aporte y devuelve la meta con su avance actualizado. */
    @PostMapping("/{id}/aportes")
    @ResponseStatus(HttpStatus.CREATED)
    public MetaResponse aportar(@PathVariable Long id, @Valid @RequestBody AporteRequest datos,
            @AuthenticationPrincipal Jwt token) {
        return metaService.aportar(id, datos, JwtService.idUsuario(token));
    }

    @DeleteMapping("/{id}/aportes/{aporteId}")
    public MetaResponse quitarAporte(@PathVariable Long id, @PathVariable Long aporteId,
            @AuthenticationPrincipal Jwt token) {
        return metaService.quitarAporte(id, aporteId, JwtService.idUsuario(token));
    }
}
