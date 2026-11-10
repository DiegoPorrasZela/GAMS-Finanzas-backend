package com.gams.finanzas.entity;

/** Cada cuánto se repite un movimiento automático. */
public enum Frecuencia {
    DIARIA,
    SEMANAL,
    /** El 15 y el último día de cada mes (pago quincenal). */
    QUINCENAL,
    MENSUAL
}
