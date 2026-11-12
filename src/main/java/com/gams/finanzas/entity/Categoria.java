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

    /** Íconos que el frontend sabe dibujar (claves separadas por "|" para validarlas con @Pattern). */
    public static final String ICONOS = "maletin|regalo|laptop|etiqueta|monedas|comida|bus|rayo|libro|salud|juego"
            + "|bolsa|caja|billetera|alcancia|retiro|casa|celular|maleta|cafe|pesa|huella|camiseta|auto";
    /** Colores de la paleta de categorías. */
    public static final String COLORES = "verde|teal|cielo|azul|indigo|violeta|rosa|rojo|naranja|ambar|gris";
    public static final String ICONO_POR_DEFECTO = "etiqueta";
    public static final String COLOR_POR_DEFECTO = "gris";

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

    @Column(nullable = false, length = 20)
    private String icono = ICONO_POR_DEFECTO;

    @Column(nullable = false, length = 20)
    private String color = COLOR_POR_DEFECTO;

    protected Categoria() {
        // Requerido por JPA
    }

    public Categoria(String nombre, TipoMovimiento tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public Categoria(String nombre, TipoMovimiento tipo, String icono, String color) {
        this(nombre, tipo);
        cambiarApariencia(icono, color);
    }

    public void actualizar(String nombre, TipoMovimiento tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
    }

    /** Ícono y color con que se muestra; si no se indica alguno, se mantiene el actual. */
    public void cambiarApariencia(String icono, String color) {
        if (icono != null) {
            this.icono = icono;
        }
        if (color != null) {
            this.color = color;
        }
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

    public String getIcono() {
        return icono;
    }

    public String getColor() {
        return color;
    }
}
