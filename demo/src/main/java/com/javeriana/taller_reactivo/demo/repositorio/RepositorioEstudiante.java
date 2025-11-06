package com.javeriana.taller_reactivo.demo.repositorio;

import com.javeriana.taller_reactivo.demo.modelo.Estudiante;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface RepositorioEstudiante extends ReactiveCrudRepository<Estudiante, Long> {
    Mono<Boolean> existsByCorreo(String email);
    Mono<Estudiante> findByCorreo(String email);
}
