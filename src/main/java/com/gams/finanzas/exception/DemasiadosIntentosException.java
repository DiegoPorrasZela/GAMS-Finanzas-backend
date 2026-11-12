package com.gams.finanzas.exception;

/** Se responde con 429: demasiados intentos seguidos desde el mismo lugar; hay que esperar. */
public class DemasiadosIntentosException extends RuntimeException {

    public DemasiadosIntentosException(String mensaje) {
        super(mensaje);
    }
}
