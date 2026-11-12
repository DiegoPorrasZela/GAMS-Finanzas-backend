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

import com.gams.finanzas.dto.RecurrenteCambioRequest;
import com.gams.finanzas.dto.RecurrenteRequest;
import com.gams.finanzas.dto.RecurrenteResponse;
import com.gams.finanzas.service.JwtService;
import com.gams.finanzas.service.RecurrenteService;

import jakarta.validation.Valid;

/** Ingresos, gastos y ahorros automáticos (diarios, semanales, quincenales o mensuales). */
@RestController
@RequestMapping("/api/recurrentes")
public class RecurrenteController {

    private final RecurrenteService recurrenteService;

    public RecurrenteController(RecurrenteService recurrenteService) {
        this.recurrenteService = recurrenteService;
    }

    @GetMapping
    public List<RecurrenteResponse> listar(@AuthenticationPrincipal Jwt token) {
        return recurrenteService.listar(JwtService.idUsuario(token));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecurrenteResponse crear(@Valid @RequestBody RecurrenteRequest datos, @AuthenticationPrincipal Jwt token) {
        return recurrenteService.crear(datos, JwtService.idUsuario(token));
    }

    @PutMapping("/{id}")
    public RecurrenteResponse actualizar(@PathVariable Long id, @Valid @RequestBody RecurrenteCambioRequest datos,
            @AuthenticationPrincipal Jwt token) {
        return recurrenteService.actualizar(id, datos, JwtService.idUsuario(token));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt token) {
        recurrenteService.eliminar(id, JwtService.idUsuario(token));
    }
}
