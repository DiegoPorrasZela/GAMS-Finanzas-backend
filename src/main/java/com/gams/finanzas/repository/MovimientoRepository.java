package com.gams.finanzas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gams.finanzas.entity.Movimiento;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    /** Movimientos de un usuario, del más reciente al más antiguo (trae la categoría en la misma consulta). */
    @EntityGraph(attributePaths = "categoria")
    List<Movimiento> findByUsuarioIdOrderByFechaDescIdDesc(Long usuarioId);

    /** Busca un movimiento solo si pertenece al usuario: así nadie puede ver o tocar los de otro. */
    Optional<Movimiento> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByCategoriaId(Long categoriaId);
}
