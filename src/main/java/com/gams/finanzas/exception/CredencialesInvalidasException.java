package com.gams.finanzas.exception;

/** Se responde con 401. El mensaje es el mismo si falla el correo o la contraseña, para no revelar cuál existe. */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Correo o contraseña incorrectos");
    }
}
