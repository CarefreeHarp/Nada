package com.javeriana.taller_reactivo.demo.repositorio; // <-- Paquete correcto

import com.javeriana.taller_reactivo.demo.modelo.Materia;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

// Asegúrate que la clase se llame igual que el archivo
public interface RepositorioMateria extends ReactiveCrudRepository<Materia, Long> {

    // Métodos requeridos por ServicioMateria
    Mono<Boolean> existsByNombre(String nombre);

    Mono<Materia> findByNombre(String nombre);
}