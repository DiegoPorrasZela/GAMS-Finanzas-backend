package com.gams.finanzas.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Calendario de los movimientos automáticos: meses de distinto largo, quincenas y años bisiestos. */
class MovimientoRecurrenteTest {

    private final Categoria sueldo = new Categoria("Sueldo", TipoMovimiento.INGRESO);

    private MovimientoRecurrente crear(Frecuencia frecuencia, Integer dia, LocalDate desde) {
        return new MovimientoRecurrente(null, sueldo, "Sueldo", new BigDecimal("650"), desde, frecuencia, dia);
    }

    /** Fechas que se registran desde "desde" hasta "hasta". */
    private List<LocalDate> fechas(Frecuencia frecuencia, Integer dia, LocalDate desde, LocalDate hasta) {
        return crear(frecuencia, dia, desde).generarHasta(hasta).stream().map(Movimiento::getFecha).toList();
    }

    private static LocalDate d(int anio, int mes, int dia) {
        return LocalDate.of(anio, mes, dia);
    }

    @Test
    @DisplayName("Último día del mes (31): se ajusta a meses de 28, 30 y 31 días")
    void ultimoDiaDelMes() {
        assertThat(fechas(Frecuencia.MENSUAL, 31, d(2026, 1, 1), d(2026, 7, 31))).containsExactly(
                d(2026, 1, 31), d(2026, 2, 28), d(2026, 3, 31), d(2026, 4, 30),
                d(2026, 5, 31), d(2026, 6, 30), d(2026, 7, 31));
    }

    @Test
    @DisplayName("Año bisiesto: en febrero de 2028 se registra el 29")
    void anioBisiesto() {
        assertThat(fechas(Frecuencia.MENSUAL, 31, d(2028, 2, 1), d(2028, 3, 31)))
                .containsExactly(d(2028, 2, 29), d(2028, 3, 31));
    }

    @Test
    @DisplayName("Día 30: en febrero usa el último día y en marzo vuelve al 30 (no al 28)")
    void dia30VuelveAl30() {
        assertThat(fechas(Frecuencia.MENSUAL, 30, d(2026, 1, 1), d(2026, 4, 30)))
                .containsExactly(d(2026, 1, 30), d(2026, 2, 28), d(2026, 3, 30), d(2026, 4, 30));
    }

    @Test
    @DisplayName("Quincenal: el 15 y el último día de cada mes")
    void quincenal() {
        assertThat(fechas(Frecuencia.QUINCENAL, null, d(2026, 1, 20), d(2026, 4, 30))).containsExactly(
                d(2026, 1, 31), d(2026, 2, 15), d(2026, 2, 28), d(2026, 3, 15),
                d(2026, 3, 31), d(2026, 4, 15), d(2026, 4, 30));
    }

    @Test
    @DisplayName("Quincenal empezando el 15: el primero es ese mismo día")
    void quincenalDesdeEl15() {
        assertThat(fechas(Frecuencia.QUINCENAL, null, d(2026, 11, 15), d(2026, 11, 30)))
                .containsExactly(d(2026, 11, 15), d(2026, 11, 30));
    }

    @Test
    @DisplayName("Programar a futuro: creado el 9 con día 15, el primero es el 15 (nada antes)")
    void programarAFuturo() {
        MovimientoRecurrente recurrente = crear(Frecuencia.MENSUAL, 15, d(2026, 11, 9));
        assertThat(recurrente.generarHasta(d(2026, 11, 9))).isEmpty();
        assertThat(recurrente.getProximoCargo()).isEqualTo(d(2026, 11, 15));
    }

    @Test
    @DisplayName("Si el día del mes ya pasó, empieza el mes siguiente")
    void diaYaPasado() {
        assertThat(fechas(Frecuencia.MENSUAL, 15, d(2026, 11, 20), d(2027, 1, 31)))
                .containsExactly(d(2026, 12, 15), d(2027, 1, 15));
    }

    @Test
    @DisplayName("Sin día indicado, se usa el día de la fecha de inicio")
    void sinDiaUsaElDeLaFecha() {
        assertThat(fechas(Frecuencia.MENSUAL, null, d(2026, 10, 9), d(2026, 12, 31)))
                .containsExactly(d(2026, 10, 9), d(2026, 11, 9), d(2026, 12, 9));
    }

    @Test
    @DisplayName("Semanal y diaria")
    void semanalYDiaria() {
        assertThat(fechas(Frecuencia.SEMANAL, null, d(2026, 11, 2), d(2026, 11, 20)))
                .containsExactly(d(2026, 11, 2), d(2026, 11, 9), d(2026, 11, 16));
        assertThat(fechas(Frecuencia.DIARIA, null, d(2026, 2, 27), d(2026, 3, 2)))
                .containsExactly(d(2026, 2, 27), d(2026, 2, 28), d(2026, 3, 1), d(2026, 3, 2));
    }

    @Test
    @DisplayName("Reanudar no registra las fechas en pausa ni repite la ya registrada")
    void reanudar() {
        MovimientoRecurrente recurrente = crear(Frecuencia.QUINCENAL, null, d(2026, 1, 1));
        recurrente.generarHasta(d(2026, 1, 15)); // registra el 15/01
        recurrente.pausar();
        recurrente.reanudar(d(2026, 3, 10)); // estuvo en pausa enero-marzo
        assertThat(recurrente.getProximoCargo()).isEqualTo(d(2026, 3, 15));
        recurrente.reanudar(d(2026, 3, 10)); // reanudar de nuevo no cambia nada
        assertThat(recurrente.getProximoCargo()).isEqualTo(d(2026, 3, 15));
    }
}
