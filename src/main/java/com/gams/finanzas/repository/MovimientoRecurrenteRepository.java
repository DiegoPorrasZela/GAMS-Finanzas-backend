package com.gams.finanzas.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gams.finanzas.entity.MovimientoRecurrente;

import jakarta.persistence.LockModeType;

public interface MovimientoRecurrenteRepository extends JpaRepository<MovimientoRecurrente, Long> {

    List<MovimientoRecurrente> findByUsuarioIdOrderByIdAsc(Long usuarioId);

    Optional<MovimientoRecurrente> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);

    /**
     * Repeticiones activas con fechas que ya llegaron. Se bloquean (SELECT ... FOR UPDATE) para que dos
     * peticiones al mismo tiempo no registren el mismo movimiento dos veces.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r FROM MovimientoRecurrente r
            WHERE r.usuario.id = :usuarioId AND r.activo = true AND r.proximoCargo <= :hoy
            """)
    List<MovimientoRecurrente> buscarPendientes(@Param("usuarioId") Long usuarioId, @Param("hoy") LocalDate hoy);
}
