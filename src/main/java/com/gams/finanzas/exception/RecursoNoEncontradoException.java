package com.gams.finanzas.exception;

/** Se responde con 404: el recurso no existe o no pertenece al usuario. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
