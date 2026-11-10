package com.gams.finanzas.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Ingreso, gasto o ahorro que se repite cada día, semana, quincena o mes (ej. el sueldo a fin de mes).
 * Cuando llega la fecha, genera el movimiento y pasa a la siguiente.
 *
 * Meses de distinto largo: si el día elegido no existe en un mes (29, 30 o 31), se usa el último día de
 * ese mes, y al mes siguiente se vuelve al día elegido. El día 31 significa "el último día del mes".
 */
@Entity
@Table(name = "movimiento_recurrente")
public class MovimientoRecurrente {

    /** Con este día se registra siempre el último día del mes (28, 29, 30 o 31). */
    public static final int ULTIMO_DIA = 31;
    private static final int QUINCENA = 15;

    /** Tope de movimientos generados de una vez (un año de repeticiones diarias). */
    private static final int MAXIMO_POR_VEZ = 400;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Frecuencia frecuencia;

    /** Día del mes en que se registra (frecuencia mensual; 31 = último día del mes). */
    @Column(nullable = false)
    private Integer dia;

    @Column(name = "proximo_cargo", nullable = false)
    private LocalDate proximoCargo;

    @Column(nullable = false)
    private Boolean activo;

    protected MovimientoRecurrente() {
        // Requerido por JPA
    }

    /**
     * @param desde desde qué día empieza: el primer registro es la primera fecha del calendario a partir de
     *              ese día (en la semanal, además, define el día de la semana)
     * @param dia   día del mes para la frecuencia mensual (1 a 31; 31 = último día). Si es null, se usa el
     *              día de "desde"
     */
    public MovimientoRecurrente(Usuario usuario, Categoria categoria, String descripcion, BigDecimal monto,
            LocalDate desde, Frecuencia frecuencia, Integer dia) {
        this.usuario = usuario;
        this.frecuencia = frecuencia;
        this.dia = frecuencia == Frecuencia.QUINCENAL ? QUINCENA : dia != null ? dia : desde.getDayOfMonth();
        this.proximoCargo = primeraDesde(desde);
        this.activo = true;
        actualizar(descripcion, monto, categoria);
    }

    public void actualizar(String descripcion, BigDecimal monto, Categoria categoria) {
        this.descripcion = descripcion;
        this.monto = monto;
        this.categoria = categoria;
    }

    /** Crea los movimientos de las fechas que ya llegaron (hasta hoy) y avanza a la siguiente. */
    public List<Movimiento> generarHasta(LocalDate hoy) {
        List<Movimiento> generados = new ArrayList<>();
        while (activo && !proximoCargo.isAfter(hoy) && generados.size() < MAXIMO_POR_VEZ) {
            generados.add(new Movimiento(usuario, descripcion, monto, proximoCargo, categoria, this));
            proximoCargo = siguiente(proximoCargo);
        }
        return generados;
    }

    public void pausar() {
        this.activo = false;
    }

    /**
     * Al reanudar no se registran las fechas que pasaron en pausa: sigue desde hoy. Tampoco retrocede,
     * para no repetir una fecha que ya se registró antes de pausar.
     */
    public void reanudar(LocalDate hoy) {
        this.activo = true;
        while (proximoCargo.isBefore(hoy)) {
            proximoCargo = siguiente(proximoCargo);
        }
    }

    /** La fecha del calendario que sigue a una ya registrada. */
    private LocalDate siguiente(LocalDate fecha) {
        return switch (frecuencia) {
            case DIARIA -> fecha.plusDays(1);
            case SEMANAL -> fecha.plusWeeks(1);
            case QUINCENAL, MENSUAL -> primeraDesde(fecha.plusDays(1));
        };
    }

    /** Primera fecha del calendario que sea igual o posterior a "desde". */
    private LocalDate primeraDesde(LocalDate desde) {
        YearMonth mes = YearMonth.from(desde);
        return switch (frecuencia) {
            case DIARIA, SEMANAL -> desde;
            case QUINCENAL -> desde.getDayOfMonth() <= QUINCENA ? mes.atDay(QUINCENA) : mes.atEndOfMonth();
            case MENSUAL -> fechaEn(mes).isBefore(desde) ? fechaEn(mes.plusMonths(1)) : fechaEn(mes);
        };
    }

    /** El día en ese mes; si el mes es más corto (ej. 31 en febrero), su último día. */
    private LocalDate fechaEn(YearMonth mes) {
        return mes.atDay(Math.min(dia, mes.lengthOfMonth()));
    }

    public Long getId() {
        return id;
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

    public Integer getDia() {
        return dia;
    }

    public Frecuencia getFrecuencia() {
        return frecuencia;
    }

    public LocalDate getProximoCargo() {
        return proximoCargo;
    }

    public boolean isActivo() {
        return activo;
    }
}
