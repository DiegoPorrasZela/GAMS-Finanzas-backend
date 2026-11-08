package com.gams.finanzas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gams.finanzas.entity.Categoria;
import com.gams.finanzas.entity.TipoMovimiento;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByOrderByTipoAscNombreAsc();

    Optional<Categoria> findByNombreAndTipo(String nombre, TipoMovimiento tipo);

    boolean existsByNombreIgnoreCaseAndTipo(String nombre, TipoMovimiento tipo);

    boolean existsByNombreIgnoreCaseAndTipoAndIdNot(String nombre, TipoMovimiento tipo, Long id);
}
