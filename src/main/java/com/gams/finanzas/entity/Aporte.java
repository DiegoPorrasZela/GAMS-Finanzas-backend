package com.gams.finanzas.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Dinero que el usuario separó para una meta de ahorro. */
@Entity
@Table(name = "aporte")
public class Aporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meta_id")
    private MetaAhorro meta;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false)
    private LocalDate fecha;

    protected Aporte() {
        // Requerido por JPA
    }

    Aporte(MetaAhorro meta, BigDecimal monto, LocalDate fecha) {
        this.meta = meta;
        this.monto = monto;
        this.fecha = fecha;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public LocalDate getFecha() {
        return fecha;
    }
}
