package com.gams.finanzas.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Ingreso o gasto registrado por un usuario.
 * No guarda el tipo: se obtiene de su categoría. Guardarlo aquí sería una
 * dependencia transitiva (id → categoria → tipo) y rompería la 3FN.
 */
@Entity
@Table(name = "movimiento")
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(nullable = false, length = 60)
    private String descripcion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false)
    private LocalDate fecha;

    /** Repetición mensual que lo generó; null si lo registró el usuario a mano. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurrente_id")
    private MovimientoRecurrente recurrente;

    /** Moneda en que se pagó (USD o EUR) si no fue en soles; el monto en soles va en "monto". */
    @Column(name = "moneda_original", length = 3)
    @JdbcTypeCode(SqlTypes.CHAR) // código ISO de 3 letras: CHAR(3) en la base de datos
    private String monedaOriginal;

    /** Monto en la moneda original (ej. 10.00 si fueron US$ 10). */
    @Column(name = "monto_original", precision = 12, scale = 2)
    private BigDecimal montoOriginal;

    protected Movimiento() {
        // Requerido por JPA
    }

    public Movimiento(Usuario usuario, String descripcion, BigDecimal monto, LocalDate fecha, Categoria categoria) {
        this(usuario, descripcion, monto, fecha, categoria, null);
    }

    public Movimiento(Usuario usuario, String descripcion, BigDecimal monto, LocalDate fecha, Categoria categoria,
            MovimientoRecurrente recurrente) {
        this.usuario = usuario;
        this.recurrente = recurrente;
        actualizar(descripcion, monto, fecha, categoria);
    }

    public boolean esAutomatico() {
        return recurrente != null;
    }

    public void actualizar(String descripcion, BigDecimal monto, LocalDate fecha, Categoria categoria) {
        this.descripcion = descripcion;
        this.monto = monto;
        this.fecha = fecha;
        this.categoria = categoria;
    }

    /** Registra en qué moneda se pagó; con null, el movimiento queda en soles. */
    public void cambiarMonedaOriginal(String moneda, BigDecimal monto) {
        this.monedaOriginal = moneda;
        this.montoOriginal = moneda == null ? null : monto;
    }

    public String getMonedaOriginal() {
        return monedaOriginal;
    }

    public BigDecimal getMontoOriginal() {
        return montoOriginal;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public TipoMovimiento getTipo() {
        return categoria.getTipo();
    }
}
