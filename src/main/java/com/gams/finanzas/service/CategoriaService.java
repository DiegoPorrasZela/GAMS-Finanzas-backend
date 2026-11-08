package com.gams.finanzas.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gams.finanzas.dto.CategoriaRequest;
import com.gams.finanzas.dto.CategoriaResponse;
import com.gams.finanzas.entity.Categoria;
import com.gams.finanzas.exception.ConflictoException;
import com.gams.finanzas.exception.RecursoNoEncontradoException;
import com.gams.finanzas.repository.CategoriaRepository;
import com.gams.finanzas.repository.MovimientoRepository;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final MovimientoRepository movimientoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, MovimientoRepository movimientoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAllByOrderByTipoAscNombreAsc().stream()
                .map(CategoriaResponse::desde)
                .toList();
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest datos) {
        String nombre = datos.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCaseAndTipo(nombre, datos.tipo())) {
            throw new ConflictoException("Ya existe la categoría \"" + nombre + "\" de tipo " + datos.tipo());
        }
        Categoria categoria = categoriaRepository.save(new Categoria(nombre, datos.tipo()));
        return CategoriaResponse.desde(categoria);
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest datos) {
        Categoria categoria = buscarEntidad(id);
        String nombre = datos.nombre().trim();

        if (categoriaRepository.existsByNombreIgnoreCaseAndTipoAndIdNot(nombre, datos.tipo(), id)) {
            throw new ConflictoException("Ya existe la categoría \"" + nombre + "\" de tipo " + datos.tipo());
        }
        // Cambiar el tipo convertiría en ingresos los gastos ya registrados (o al revés)
        if (categoria.getTipo() != datos.tipo() && movimientoRepository.existsByCategoriaId(id)) {
            throw new ConflictoException("No se puede cambiar el tipo de una categoría que ya tiene movimientos");
        }

        categoria.actualizar(nombre, datos.tipo());
        return CategoriaResponse.desde(categoria);
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = buscarEntidad(id);
        if (movimientoRepository.existsByCategoriaId(id)) {
            throw new ConflictoException("No se puede eliminar una categoría que ya tiene movimientos");
        }
        categoriaRepository.delete(categoria);
    }

    /** Usado también por MovimientoService para validar la categoría elegida. */
    @Transactional(readOnly = true)
    public Categoria buscarEntidad(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La categoría " + id + " no existe"));
    }
}
