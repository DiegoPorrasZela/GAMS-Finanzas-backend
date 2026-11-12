package com.gams.finanzas.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gams.finanzas.dto.AporteRequest;
import com.gams.finanzas.dto.MetaRequest;
import com.gams.finanzas.dto.MetaResponse;
import com.gams.finanzas.entity.MetaAhorro;
import com.gams.finanzas.exception.ConflictoException;
import com.gams.finanzas.exception.RecursoNoEncontradoException;
import com.gams.finanzas.repository.MetaAhorroRepository;
import com.gams.finanzas.repository.UsuarioRepository;

/**
 * CRUD de metas de ahorro y de sus aportes. Cada usuario solo accede a sus propias metas.
 * Los aportes salen de los ahorros del usuario (no directamente de su saldo).
 */
@Service
public class MetaAhorroService {

    private final MetaAhorroRepository metaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AhorroService ahorroService;

    public MetaAhorroService(MetaAhorroRepository metaRepository, UsuarioRepository usuarioRepository,
            AhorroService ahorroService) {
        this.metaRepository = metaRepository;
        this.usuarioRepository = usuarioRepository;
        this.ahorroService = ahorroService;
    }

    @Transactional(readOnly = true)
    public List<MetaResponse> listar(Long usuarioId) {
        return metaRepository.findByUsuarioIdOrderByIdAsc(usuarioId).stream()
                .map(MetaResponse::desde)
                .toList();
    }

    @Transactional
    public MetaResponse crear(MetaRequest datos, Long usuarioId) {
        MetaAhorro meta = new MetaAhorro(usuarioRepository.getReferenceById(usuarioId), datos.nombre().trim(),
                datos.montoObjetivo(), datos.fechaLimite());
        return MetaResponse.desde(metaRepository.save(meta));
    }

    @Transactional
    public MetaResponse actualizar(Long id, MetaRequest datos, Long usuarioId) {
        MetaAhorro meta = buscarPropia(id, usuarioId);
        // El objetivo no puede quedar por debajo de lo que ya se ahorró (la meta pasaría del 100 %)
        BigDecimal ahorrado = meta.getAhorrado();
        if (datos.montoObjetivo().compareTo(ahorrado) < 0) {
            throw new ConflictoException("Ya ahorraste " + AhorroService.soles(ahorrado)
                    + " en esta meta: el objetivo no puede ser menor. Quita primero algún aporte.");
        }
        meta.actualizar(datos.nombre().trim(), datos.montoObjetivo(), datos.fechaLimite());
        return MetaResponse.desde(meta);
    }

    /** Al eliminar la meta, lo que se había aportado vuelve a estar libre en los ahorros. */
    @Transactional
    public void eliminar(Long id, Long usuarioId) {
        metaRepository.delete(buscarPropia(id, usuarioId));
    }

    /** Pasa dinero de los ahorros a una meta. No se puede aportar más de lo que hay libre en los ahorros. */
    @Transactional
    public MetaResponse aportar(Long metaId, AporteRequest datos, Long usuarioId) {
        MetaAhorro meta = buscarPropia(metaId, usuarioId);
        // Una meta cumplida no recibe más: el aporte no puede pasar de lo que falta
        BigDecimal faltante = meta.getMontoObjetivo().subtract(meta.getAhorrado());
        if (faltante.signum() <= 0) {
            throw new ConflictoException("Esta meta ya está cumplida: no necesita más aportes.");
        }
        if (datos.monto().compareTo(faltante) > 0) {
            throw new ConflictoException("Solo te faltan " + AhorroService.soles(faltante)
                    + " para cumplir esta meta: no puedes aportar más que eso.");
        }
        BigDecimal libres = ahorroService.libres(usuarioId);
        if (datos.monto().compareTo(libres) > 0) {
            throw new ConflictoException(libres.signum() > 0
                    ? "En tus ahorros tienes " + AhorroService.soles(libres)
                            + " libres. Separa más dinero a tus ahorros para aportar esa cantidad."
                    : "Tus ahorros están vacíos: primero separa dinero de tu saldo a tus ahorros.");
        }
        meta.aportar(datos.monto(), datos.fecha());
        metaRepository.flush(); // guarda el aporte ahora para que la respuesta ya tenga su id
        return MetaResponse.desde(meta);
    }

    /** El aporte quitado vuelve a estar libre en los ahorros. */
    @Transactional
    public MetaResponse quitarAporte(Long metaId, Long aporteId, Long usuarioId) {
        MetaAhorro meta = buscarPropia(metaId, usuarioId);
        if (!meta.quitarAporte(aporteId)) {
            throw new RecursoNoEncontradoException("El aporte " + aporteId + " no existe en esta meta");
        }
        return MetaResponse.desde(meta);
    }

    private MetaAhorro buscarPropia(Long id, Long usuarioId) {
        return metaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La meta " + id + " no existe"));
    }
}
