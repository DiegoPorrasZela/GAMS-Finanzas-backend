package com.gams.finanzas.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gams.finanzas.entity.MetaAhorro;

public interface MetaAhorroRepository extends JpaRepository<MetaAhorro, Long> {

    /** Metas del usuario con sus aportes en la misma consulta. */
    @EntityGraph(attributePaths = "aportes")
    List<MetaAhorro> findByUsuarioIdOrderByIdAsc(Long usuarioId);

    /** Solo si la meta pertenece al usuario. */
    @EntityGraph(attributePaths = "aportes")
    Optional<MetaAhorro> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);

    /** Todo lo que el usuario ya separó en sus metas. */
    @Query("SELECT COALESCE(SUM(a.monto), 0) FROM MetaAhorro m JOIN m.aportes a WHERE m.usuario.id = :usuarioId")
    BigDecimal totalAportado(@Param("usuarioId") Long usuarioId);
}
