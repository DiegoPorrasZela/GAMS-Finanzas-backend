package com.gams.finanzas.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Ruta pública para comprobar que el API está en línea (y para "despertar" el servidor gratuito). */
@RestController
public class EstadoController {

    @GetMapping("/api/estado")
    public Map<String, String> estado() {
        return Map.of("estado", "en línea", "aplicacion", "GAMS Finanzas API");
    }
}
