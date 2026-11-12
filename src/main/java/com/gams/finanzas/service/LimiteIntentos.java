package com.gams.finanzas.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.gams.finanzas.exception.DemasiadosIntentosException;

/**
 * Frena los intentos repetidos (adivinar contraseñas, crear cuentas en masa).
 * Cuenta los intentos por clave (ej. "login:IP:correo") dentro de una ventana de tiempo; al pasar el
 * máximo, rechaza hasta que la ventana termine. Se guarda en memoria: basta para un solo servidor y se
 * reinicia con él.
 */
@Component
public class LimiteIntentos {

    /** Cuántos intentos van y cuándo se vuelve a empezar de cero. */
    private record Ventana(int intentos, Instant termina) {
    }

    private static final int LIMPIAR_AL_SUPERAR = 10_000;

    private final Map<String, Ventana> ventanas = new ConcurrentHashMap<>();

    /** Lanza la excepción si la clave ya llegó al máximo y su ventana sigue abierta. */
    public void verificar(String clave, int maximo) {
        Ventana ventana = ventanas.get(clave);
        if (ventana == null) {
            return;
        }
        Instant ahora = Instant.now();
        if (!ventana.termina().isAfter(ahora)) {
            ventanas.remove(clave, ventana);
            return;
        }
        if (ventana.intentos() >= maximo) {
            long minutos = Math.max(1, Duration.between(ahora, ventana.termina()).toMinutes() + 1);
            throw new DemasiadosIntentosException("Demasiados intentos. Vuelve a intentarlo en "
                    + minutos + (minutos == 1 ? " minuto." : " minutos."));
        }
    }

    /** Suma un intento; el primero abre la ventana. */
    public void registrar(String clave, Duration duracion) {
        Instant ahora = Instant.now();
        if (ventanas.size() > LIMPIAR_AL_SUPERAR) {
            ventanas.values().removeIf(ventana -> !ventana.termina().isAfter(ahora));
        }
        ventanas.merge(clave, new Ventana(1, ahora.plus(duracion)),
                (actual, nueva) -> actual.termina().isAfter(ahora)
                        ? new Ventana(actual.intentos() + 1, actual.termina())
                        : nueva);
    }

    /** Olvida los intentos (ej. después de iniciar sesión correctamente). */
    public void olvidar(String clave) {
        ventanas.remove(clave);
    }
}
