package com.gams.finanzas.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gams.finanzas.dto.PresupuestoRequest;
import com.gams.finanzas.dto.PresupuestoResponse;
import com.gams.finanzas.entity.PresupuestoMensual;
import com.gams.finanzas.exception.DatosInvalidosException;
import com.gams.finanzas.exception.RecursoNoEncontradoException;
import com.gams.finanzas.repository.PresupuestoMensualRepository;
import com.gams.finanzas.repository.UsuarioRepository;

/** Presupuesto y meta de ahorro de cada mes. Cada usuario tiene como máximo un plan por mes. */
@Service
public class PresupuestoService {

    private final PresupuestoMensualRepository presupuestoRepository;
    private final UsuarioRepository usuarioRepository;

    public PresupuestoService(PresupuestoMensualRepository presupuestoRepository,
            UsuarioRepository usuarioRepository) {
        this.presupuestoRepository = presupuestoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<PresupuestoResponse> listar(Long usuarioId) {
        return presupuestoRepository.findByUsuarioIdOrderByAnioDescMesDesc(usuarioId).stream()
                .map(PresupuestoResponse::desde)
                .toList();
    }

    /** Crea el plan del mes o lo actualiza si ya existía (PUT idempotente). */
    @Transactional
    public PresupuestoResponse guardar(int anio, int mes, PresupuestoRequest datos, Long usuarioId) {
        validarMes(anio, mes);
        PresupuestoMensual presupuesto = presupuestoRepository.findByUsuarioIdAndAnioAndMes(usuarioId, anio, mes)
                .map(existente -> {
                    existente.actualizar(datos.limiteGasto(), datos.ahorroObjetivo());
                    return existente;
                })
                .orElseGet(() -> presupuestoRepository.save(new PresupuestoMensual(
                        usuarioRepository.getReferenceById(usuarioId), anio, mes,
                        datos.limiteGasto(), datos.ahorroObjetivo())));
        return PresupuestoResponse.desde(presupuesto);
    }

    @Transactional
    public void eliminar(int anio, int mes, Long usuarioId) {
        validarMes(anio, mes);
        PresupuestoMensual presupuesto = presupuestoRepository.findByUsuarioIdAndAnioAndMes(usuarioId, anio, mes)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No tienes un presupuesto para " + mes + "/" + anio));
        presupuestoRepository.delete(presupuesto);
    }

    private static void validarMes(int anio, int mes) {
        if (mes < 1 || mes > 12 || anio < 2000 || anio > 2100) {
            throw new DatosInvalidosException("El mes " + mes + "/" + anio + " no es válido");
        }
    }
}
