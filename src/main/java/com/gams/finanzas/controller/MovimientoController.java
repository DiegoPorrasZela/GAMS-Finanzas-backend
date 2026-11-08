package com.gams.finanzas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.gams.finanzas.config.DatosIniciales;
import com.gams.finanzas.dto.MovimientoRequest;
import com.gams.finanzas.dto.MovimientoResponse;
import com.gams.finanzas.repository.UsuarioRepository;
import com.gams.finanzas.service.MovimientoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    private final MovimientoService movimientoService;
    private final UsuarioRepository usuarioRepository;

    public MovimientoController(MovimientoService movimientoService, UsuarioRepository usuarioRepository) {
        this.movimientoService = movimientoService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public List<MovimientoResponse> listar() {
        return movimientoService.listar(usuarioActualId());
    }

    @GetMapping("/{id}")
    public MovimientoResponse obtener(@PathVariable Long id) {
        return movimientoService.obtener(id, usuarioActualId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoResponse crear(@Valid @RequestBody MovimientoRequest datos) {
        return movimientoService.crear(datos, usuarioActualId());
    }

    @PutMapping("/{id}")
    public MovimientoResponse actualizar(@PathVariable Long id, @Valid @RequestBody MovimientoRequest datos) {
        return movimientoService.actualizar(id, datos, usuarioActualId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        movimientoService.eliminar(id, usuarioActualId());
    }

    // TEMPORAL (Fase 1): mientras no hay login, todo se hace como el usuario demo.
    // En la Fase 2 se reemplaza por el usuario autenticado con JWT.
    private Long usuarioActualId() {
        return usuarioRepository.findByEmail(DatosIniciales.EMAIL_DEMO).orElseThrow().getId();
    }
}
