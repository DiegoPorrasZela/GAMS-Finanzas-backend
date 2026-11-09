package com.gams.finanzas.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Objetivo de ahorro de un usuario (ej. "Laptop", S/ 2500).
 * Lo ahorrado no se guarda: es la suma de sus aportes. La meta controla sus aportes
 * (se crean y eliminan a través de ella).
 */
@Entity
@Table(name = "meta_ahorro")
public class MetaAhorro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(name = "monto_objetivo", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoObjetivo;

    @Column(name = "fecha_limite")
    private LocalDate fechaLimite;

    @OneToMany(mappedBy = "meta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Aporte> aportes = new ArrayList<>();

    protected MetaAhorro() {
        // Requerido por JPA
    }

    public MetaAhorro(Usuario usuario, String nombre, BigDecimal montoObjetivo, LocalDate fechaLimite) {
        this.usuario = usuario;
        actualizar(nombre, montoObjetivo, fechaLimite);
    }

    public void actualizar(String nombre, BigDecimal montoObjetivo, LocalDate fechaLimite) {
        this.nombre = nombre;
        this.montoObjetivo = montoObjetivo;
        this.fechaLimite = fechaLimite;
    }

    public Aporte aportar(BigDecimal monto, LocalDate fecha) {
        Aporte aporte = new Aporte(this, monto, fecha);
        aportes.add(aporte);
        return aporte;
    }

    /** Devuelve false si el aporte no pertenece a esta meta. */
    public boolean quitarAporte(Long aporteId) {
        return aportes.removeIf(aporte -> aporte.getId().equals(aporteId));
    }

    public BigDecimal getAhorrado() {
        return aportes.stream().map(Aporte::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getMontoObjetivo() {
        return montoObjetivo;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public List<Aporte> getAportes() {
        return aportes;
    }
}
