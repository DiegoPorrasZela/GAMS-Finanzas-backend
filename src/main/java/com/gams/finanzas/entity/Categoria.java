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

    // Categorías del sistema: sostienen reglas de negocio, así que no se pueden renombrar ni eliminar
    /** Dinero con el que empieza el usuario (una sola vez por usuario). */
    public static final String SALDO_INICIAL = "Saldo inicial";
    /** Dinero que sale del saldo hacia los ahorros (no es un gasto: es dinero guardado). */
    public static final String AHORRO = "Ahorro";
    /** Dinero que vuelve de los ahorros al saldo. */
    public static final String RETIRO_AHORROS = "Retiro de ahorros";

    public static boolean esNombreDelSistema(String nombre) {
        return SALDO_INICIAL.equalsIgnoreCase(nombre) || AHORRO.equalsIgnoreCase(nombre)
                || RETIRO_AHORROS.equalsIgnoreCase(nombre);
    }

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

    public boolean esAhorro() {
        return tipo == TipoMovimiento.GASTO && AHORRO.equals(nombre);
    }

    public boolean esRetiroAhorros() {
        return tipo == TipoMovimiento.INGRESO && RETIRO_AHORROS.equals(nombre);
    }

    public boolean esDelSistema() {
        return esSaldoInicial() || esAhorro() || esRetiroAhorros();
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
