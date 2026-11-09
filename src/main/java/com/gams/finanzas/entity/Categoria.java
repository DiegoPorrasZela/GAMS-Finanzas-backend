package com.gams.finanzas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Categoría de un movimiento. Su tipo (ingreso o gasto) define el tipo de todos sus movimientos. */
@Entity
@Table(name = "categoria")
public class Categoria {

    /** Categoría del sistema para el dinero con el que empieza el usuario (una sola vez por usuario). */
    public static final String SALDO_INICIAL = "Saldo inicial";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimiento tipo;

    protected Categoria() {
        // Requerido por JPA
    }

    public Categoria(String nombre, TipoMovimiento tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public void actualizar(String nombre, TipoMovimiento tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public boolean esSaldoInicial() {
        return tipo == TipoMovimiento.INGRESO && SALDO_INICIAL.equals(nombre);
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }
}
