package com.gams.finanzas.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gams.finanzas.dto.UsuarioResponse;
import com.gams.finanzas.entity.Usuario;
import com.gams.finanzas.exception.ConflictoException;
import com.gams.finanzas.exception.RecursoNoEncontradoException;
import com.gams.finanzas.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        // Usuario::getId en vez de "id": si la propiedad cambia de nombre, falla al compilar y no en ejecución
        return usuarioRepository.findAll(Sort.by(Usuario::getId)).stream()
                .map(UsuarioResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.desde(buscar(id));
    }

    /** Elimina la cuenta y, por la clave foránea con ON DELETE CASCADE, todos sus movimientos. */
    @Transactional
    public void eliminar(Long id, Long idAdministrador) {
        if (id.equals(idAdministrador)) {
            throw new ConflictoException("No puedes eliminar tu propia cuenta");
        }
        usuarioRepository.delete(buscar(id));
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario " + id + " no existe"));
    }
}
