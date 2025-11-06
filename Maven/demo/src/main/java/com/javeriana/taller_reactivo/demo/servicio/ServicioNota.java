package com.javeriana.taller_reactivo.demo.servicio;

import com.javeriana.taller_reactivo.demo.modelo.Nota;
import com.javeriana.taller_reactivo.demo.repositorio.RepositorioNota;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional
public class ServicioNota {

    private final RepositorioNota repo;

    public ServicioNota(RepositorioNota repo) {
        this.repo = repo;
    }

    // ----------------- CRUD -----------------

    public Flux<Nota> listar() {
        return repo.findAll();
    }

    public Flux<Nota> listarPorEstudiante(Long estudianteId) {
        return repo.findAllByEstudianteId(estudianteId);
    }

    public Flux<Nota> listarPorMateria(Long materiaId) {
        return repo.findAllByMateriaId(materiaId);
    }

    public Flux<Nota> listarPorEstudianteMateria(Long estudianteId, Long materiaId) {
        return repo.findByEstudianteIdAndMateriaId(estudianteId, materiaId);
    }

    public Mono<Nota> crear(Nota n) {
        return validarRangos(n)
                .then(validarPorcentajeDisponible(n.getEstudianteId(), n.getMateriaId(), n.getPorcentaje(), null))
                .then(repo.save(n));
    }

    public Mono<Nota> actualizar(Long id, Nota cambios) {
        return repo.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("La nota no existe")))
                .flatMap(actual -> validarRangos(cambios)
                        .then(validarPorcentajeDisponible(
                                actual.getEstudianteId(),
                                actual.getMateriaId(),
                                cambios.getPorcentaje(),
                                id
                        ))
                        .then(Mono.defer(() -> {
                            actual.setObservacion(cambios.getObservacion());
                            actual.setValor(cambios.getValor());
                            actual.setPorcentaje(cambios.getPorcentaje());
                            return repo.save(actual);
                        }))
                );
    }

    public Mono<Void> eliminar(Long id) {
        return repo.deleteById(id);
    }

    // ----------------- Lógica de negocio -----------------

    /** Calcula el promedio ponderado (0..5) de un estudiante en una materia */
    public Mono<BigDecimal> promedioPonderado(Long estudianteId, Long materiaId) {
        return repo.findByEstudianteIdAndMateriaId(estudianteId, materiaId)
                .reduce(new double[]{0.0, 0.0}, (acc, nota) -> {
                    acc[0] += nota.getValor() * (nota.getPorcentaje() / 100.0);
                    acc[1] += nota.getPorcentaje();
                    return acc;
                })
                .map(acc -> BigDecimal.valueOf(acc[0]).setScale(2, RoundingMode.HALF_UP));
    }

    /** Valida que los porcentajes no superen el 100% */
    private Mono<Void> validarPorcentajeDisponible(Long estudianteId, Long materiaId,
                                                   double porcentajeNuevo, Long excluirNotaId) {
        return repo.findByEstudianteIdAndMateriaId(estudianteId, materiaId)
                .filter(n -> excluirNotaId == null || !n.getId().equals(excluirNotaId))
                .map(Nota::getPorcentaje)
                .reduce(0.0, Double::sum)
                .flatMap(total -> (total + porcentajeNuevo) > 100.0
                        ? Mono.error(new IllegalArgumentException(
                        "Los porcentajes exceden 100% (acumulado: " + total + ", nuevo: " + porcentajeNuevo + ")"))
                        : Mono.empty());
    }


    /** Valida rangos de nota y porcentaje */
    private Mono<Void> validarRangos(Nota n) {
        // Validar valor de la nota (entre 0.0 y 5.0)
        if (n.getValor() < 0.0 || n.getValor() > 5.0) {
            return Mono.error(new IllegalArgumentException("La nota debe estar entre 0.0 y 5.0"));
        }

        // Validar porcentaje (entre 0 y 100)
        if (n.getPorcentaje() < 0.0 || n.getPorcentaje() > 100.0) {
            return Mono.error(new IllegalArgumentException("El porcentaje debe estar entre 0 y 100"));
        }

        return Mono.empty();
    }

}
