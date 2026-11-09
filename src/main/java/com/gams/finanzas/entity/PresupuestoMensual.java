package com.gams.finanzas.entity;

import java.math.BigDecimal;

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
 * Plan de un usuario para un mes: cuánto quiere gastar como máximo y cuánto quiere ahorrar.
 * Lo gastado no se guarda aquí: se calcula a partir de los movimientos del mes.
 */
@Entity
@Table(name = "presupuesto_mensual")
public class PresupuestoMensual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false)
    private Integer anio;

    @Column(nullable = false)
    private Integer mes;

    @Column(name = "limite_gasto", nullable = false, precision = 12, scale = 2)
    private BigDecimal limiteGasto;

    @Column(name = "ahorro_objetivo", nullable = false, precision = 12, scale = 2)
    private BigDecimal ahorroObjetivo;

    protected PresupuestoMensual() {
        // Requerido por JPA
    }

    public PresupuestoMensual(Usuario usuario, int anio, int mes, BigDecimal limiteGasto, BigDecimal ahorroObjetivo) {
        this.usuario = usuario;
        this.anio = anio;
        this.mes = mes;
        actualizar(limiteGasto, ahorroObjetivo);
    }

    public void actualizar(BigDecimal limiteGasto, BigDecimal ahorroObjetivo) {
        this.limiteGasto = limiteGasto;
        this.ahorroObjetivo = ahorroObjetivo;
    }

    public Long getId() {
        return id;
    }

    public Integer getAnio() {
        return anio;
    }

    public Integer getMes() {
        return mes;
    }

    public BigDecimal getLimiteGasto() {
        return limiteGasto;
    }

    public BigDecimal getAhorroObjetivo() {
        return ahorroObjetivo;
    }
}
