package com.gams.finanzas.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gams.finanzas.dto.AporteRequest;
import com.gams.finanzas.dto.MetaRequest;
import com.gams.finanzas.dto.MetaResponse;
import com.gams.finanzas.entity.MetaAhorro;
import com.gams.finanzas.entity.TipoMovimiento;
import com.gams.finanzas.exception.ConflictoException;
import com.gams.finanzas.exception.RecursoNoEncontradoException;
import com.gams.finanzas.repository.MetaAhorroRepository;
import com.gams.finanzas.repository.MovimientoRepository;
import com.gams.finanzas.repository.UsuarioRepository;

/** CRUD de metas de ahorro y de sus aportes. Cada usuario solo accede a sus propias metas. */
@Service
public class MetaAhorroService {

    private final MetaAhorroRepository metaRepository;
    private final MovimientoRepository movimientoRepository;
    private final UsuarioRepository usuarioRepository;

    public MetaAhorroService(MetaAhorroRepository metaRepository, MovimientoRepository movimientoRepository,
            UsuarioRepository usuarioRepository) {
        this.metaRepository = metaRepository;
        this.movimientoRepository = movimientoRepository;
        this.usuarioRepository = usuarioRepository;
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
        meta.actualizar(datos.nombre().trim(), datos.montoObjetivo(), datos.fechaLimite());
        return MetaResponse.desde(meta);
    }

    @Transactional
    public void eliminar(Long id, Long usuarioId) {
        metaRepository.delete(buscarPropia(id, usuarioId));
    }

    /**
     * Separa dinero para una meta. No se puede separar más de lo disponible:
     * disponible = saldo (ingresos − gastos) − lo que ya está separado en otras metas.
     */
    @Transactional
    public MetaResponse aportar(Long metaId, AporteRequest datos, Long usuarioId) {
        MetaAhorro meta = buscarPropia(metaId, usuarioId);
        BigDecimal disponible = disponible(usuarioId);
        if (datos.monto().compareTo(disponible) > 0) {
            throw new ConflictoException(disponible.signum() > 0
                    ? "Solo tienes S/ %.2f disponibles para separar (tu saldo menos lo que ya está en tus metas)"
                            .formatted(disponible)
                    : "No tienes saldo disponible para separar: registra primero tus ingresos");
        }
        meta.aportar(datos.monto(), datos.fecha());
        metaRepository.flush(); // guarda el aporte ahora para que la respuesta ya tenga su id
        return MetaResponse.desde(meta);
    }

    @Transactional
    public MetaResponse quitarAporte(Long metaId, Long aporteId, Long usuarioId) {
        MetaAhorro meta = buscarPropia(metaId, usuarioId);
        if (!meta.quitarAporte(aporteId)) {
            throw new RecursoNoEncontradoException("El aporte " + aporteId + " no existe en esta meta");
        }
        return MetaResponse.desde(meta);
    }

    private BigDecimal disponible(Long usuarioId) {
        BigDecimal saldo = movimientoRepository.sumarPorTipo(usuarioId, TipoMovimiento.INGRESO)
                .subtract(movimientoRepository.sumarPorTipo(usuarioId, TipoMovimiento.GASTO));
        return saldo.subtract(metaRepository.totalAportado(usuarioId));
    }

    private MetaAhorro buscarPropia(Long id, Long usuarioId) {
        return metaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La meta " + id + " no existe"));
    }
}
