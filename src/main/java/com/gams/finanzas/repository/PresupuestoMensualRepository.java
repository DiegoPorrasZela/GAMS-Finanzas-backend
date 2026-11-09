package com.gams.finanzas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gams.finanzas.entity.PresupuestoMensual;

public interface PresupuestoMensualRepository extends JpaRepository<PresupuestoMensual, Long> {

    List<PresupuestoMensual> findByUsuarioIdOrderByAnioDescMesDesc(Long usuarioId);

    Optional<PresupuestoMensual> findByUsuarioIdAndAnioAndMes(Long usuarioId, Integer anio, Integer mes);

    boolean existsByUsuarioId(Long usuarioId);
}
