package com.gams.finanzas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Rol de un usuario. Define qué puede hacer dentro de la aplicación (autorización). */
@Entity
@Table(name = "rol")
public class Rol {

    public static final String ADMIN = "ADMIN";
    public static final String USUARIO = "USUARIO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String nombre;

    protected Rol() {
        // Requerido por JPA
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
