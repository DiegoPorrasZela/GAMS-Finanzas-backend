package com.gams.finanzas.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.gams.finanzas.exception.DemasiadosIntentosException;

class LimiteIntentosTest {

    private static final Duration QUINCE_MINUTOS = Duration.ofMinutes(15);

    private final LimiteIntentos limite = new LimiteIntentos();

    @Test
    @DisplayName("Deja pasar hasta el máximo y luego bloquea esa clave (no las demás)")
    void bloqueaAlLlegarAlMaximo() {
        for (int i = 0; i < 3; i++) {
            limite.verificar("login:1.1.1.1:ana@x.pe", 3);
            limite.registrar("login:1.1.1.1:ana@x.pe", QUINCE_MINUTOS);
        }
        assertThatThrownBy(() -> limite.verificar("login:1.1.1.1:ana@x.pe", 3))
                .isInstanceOf(DemasiadosIntentosException.class)
                .hasMessageContaining("15 minutos");
        assertThatCode(() -> limite.verificar("login:2.2.2.2:ana@x.pe", 3)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Al olvidar (login correcto) se empieza de cero")
    void olvidar() {
        limite.registrar("clave", QUINCE_MINUTOS);
        limite.registrar("clave", QUINCE_MINUTOS);
        limite.olvidar("clave");
        assertThatCode(() -> limite.verificar("clave", 2)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Cuando la ventana termina, los intentos anteriores ya no cuentan")
    void ventanaVencida() {
        limite.registrar("clave", Duration.ZERO);
        assertThatCode(() -> limite.verificar("clave", 1)).doesNotThrowAnyException();
    }
}
