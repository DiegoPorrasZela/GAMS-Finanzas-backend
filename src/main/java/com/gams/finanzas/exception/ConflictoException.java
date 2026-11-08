package com.gams.finanzas.exception;

/** Se responde con 409: la operación choca con una regla del negocio (duplicado, en uso, etc.). */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
