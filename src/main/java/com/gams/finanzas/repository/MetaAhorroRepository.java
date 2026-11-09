package com.gams.finanzas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gams.finanzas.entity.MetaAhorro;

public interface MetaAhorroRepository extends JpaRepository<MetaAhorro, Long> {

    /** Metas del usuario con sus aportes en la misma consulta. */
    @EntityGraph(attributePaths = "aportes")
    List<MetaAhorro> findByUsuarioIdOrderByIdAsc(Long usuarioId);

    /** Solo si la meta pertenece al usuario. */
    @EntityGraph(attributePaths = "aportes")
    Optional<MetaAhorro> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);
}
