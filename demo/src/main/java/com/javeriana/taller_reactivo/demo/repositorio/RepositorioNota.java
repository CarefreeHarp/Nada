package com.javeriana.taller_reactivo.demo.repositorio;

import com.javeriana.taller_reactivo.demo.modelo.Nota;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface RepositorioNota extends ReactiveCrudRepository<Nota, Long> {
    Flux<Nota> findAllByMateriaId(Long materiaId);
    Flux<Nota> findAllByEstudianteId(Long estudianteId);
    Flux<Nota> findByEstudianteIdAndMateriaId(Long estudianteId, Long materiaId);
}
