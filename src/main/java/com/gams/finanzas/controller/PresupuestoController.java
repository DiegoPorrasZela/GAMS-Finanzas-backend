package com.gams.finanzas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.gams.finanzas.dto.PresupuestoRequest;
import com.gams.finanzas.dto.PresupuestoResponse;
import com.gams.finanzas.service.JwtService;
import com.gams.finanzas.service.PresupuestoService;

import jakarta.validation.Valid;

/** Plan del mes del usuario. El recurso se identifica por año y mes: PUT crea o actualiza. */
@RestController
@RequestMapping("/api/presupuestos")
public class PresupuestoController {

    private final PresupuestoService presupuestoService;

    public PresupuestoController(PresupuestoService presupuestoService) {
        this.presupuestoService = presupuestoService;
    }

    @GetMapping
    public List<PresupuestoResponse> listar(@AuthenticationPrincipal Jwt token) {
        return presupuestoService.listar(JwtService.idUsuario(token));
    }

    @PutMapping("/{anio}/{mes}")
    public PresupuestoResponse guardar(@PathVariable int anio, @PathVariable int mes,
            @Valid @RequestBody PresupuestoRequest datos, @AuthenticationPrincipal Jwt token) {
        return presupuestoService.guardar(anio, mes, datos, JwtService.idUsuario(token));
    }

    @DeleteMapping("/{anio}/{mes}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable int anio, @PathVariable int mes, @AuthenticationPrincipal Jwt token) {
        presupuestoService.eliminar(anio, mes, JwtService.idUsuario(token));
    }
}
