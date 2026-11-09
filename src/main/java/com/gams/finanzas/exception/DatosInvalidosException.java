package com.gams.finanzas.exception;

/** Se responde con 400: un dato de la petición no es válido (ej. un mes que no existe). */
public class DatosInvalidosException extends RuntimeException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
