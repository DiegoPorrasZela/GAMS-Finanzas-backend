package com.gams.finanzas.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gams.finanzas.entity.Movimiento;
import com.gams.finanzas.entity.TipoMovimiento;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    /** Movimientos de un usuario, del más reciente al más antiguo (trae la categoría en la misma consulta). */
    @EntityGraph(attributePaths = "categoria")
    List<Movimiento> findByUsuarioIdOrderByFechaDescIdDesc(Long usuarioId);

    /** Busca un movimiento solo si pertenece al usuario: así nadie puede ver o tocar los de otro. */
    Optional<Movimiento> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByCategoriaId(Long categoriaId);

    boolean existsByUsuarioIdAndCategoriaId(Long usuarioId, Long categoriaId);

    boolean existsByUsuarioIdAndCategoriaIdAndIdNot(Long usuarioId, Long categoriaId, Long id);

    /** Total de ingresos o de gastos de un usuario (para calcular su saldo). */
    @Query("""
            SELECT COALESCE(SUM(m.monto), 0) FROM Movimiento m
            WHERE m.usuario.id = :usuarioId AND m.categoria.tipo = :tipo
            """)
    BigDecimal sumarPorTipo(@Param("usuarioId") Long usuarioId, @Param("tipo") TipoMovimiento tipo);
}
