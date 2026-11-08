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

import com.gams.finanzas.dto.MovimientoRequest;
import com.gams.finanzas.dto.MovimientoResponse;
import com.gams.finanzas.service.JwtService;
import com.gams.finanzas.service.MovimientoService;

import jakarta.validation.Valid;

/** CRUD de movimientos. El usuario dueño siempre sale del token, nunca de lo que envía el cliente. */
@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    private final MovimientoService movimientoService;

    public MovimientoController(MovimientoService movimientoService) {
        this.movimientoService = movimientoService;
    }

    @GetMapping
    public List<MovimientoResponse> listar(@AuthenticationPrincipal Jwt token) {
        return movimientoService.listar(JwtService.idUsuario(token));
    }

    @GetMapping("/{id}")
    public MovimientoResponse obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt token) {
        return movimientoService.obtener(id, JwtService.idUsuario(token));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoResponse crear(@Valid @RequestBody MovimientoRequest datos, @AuthenticationPrincipal Jwt token) {
        return movimientoService.crear(datos, JwtService.idUsuario(token));
    }

    @PutMapping("/{id}")
    public MovimientoResponse actualizar(@PathVariable Long id, @Valid @RequestBody MovimientoRequest datos,
            @AuthenticationPrincipal Jwt token) {
        return movimientoService.actualizar(id, datos, JwtService.idUsuario(token));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt token) {
        movimientoService.eliminar(id, JwtService.idUsuario(token));
    }
}
