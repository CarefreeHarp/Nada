package com.javeriana.taller_reactivo.demo.servicio;

import com.javeriana.taller_reactivo.demo.modelo.Estudiante;
import com.javeriana.taller_reactivo.demo.modelo.Nota;
import com.javeriana.taller_reactivo.demo.repositorio.RepositorioEstudiante;
import com.javeriana.taller_reactivo.demo.repositorio.RepositorioNota;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@Service
@Transactional
public class ServicioEstudiante {

    private final RepositorioEstudiante repo;
    private final RepositorioNota repoNota; // ¡AÑADIR ESTO!

    // ¡ACTUALIZAR CONSTRUCTOR!
    public ServicioEstudiante(RepositorioEstudiante repo, RepositorioNota repoNota) {
        this.repo = repo;
        this.repoNota = repoNota;
    }
    public Flux<Estudiante> listar() {
        return repo.findAll();
    }

    public Mono<Estudiante> porId(Long id) {
        return repo.findById(id);
    }

    public Mono<Estudiante> crear(Estudiante e) {
        return repo.existsByCorreo(e.getCorreo())
                .flatMap(existe -> existe
                        ? Mono.error(new IllegalArgumentException("El correo ya está registrado"))
                        : repo.save(e));
    }

// ... (importaciones y otros métodos)

// --- MÉTODO 'actualizar' CORREGIDO ---
public Mono<Estudiante> actualizar(Long id, Estudiante cambios) {
    return repo.findById(id)
        // 1. Si findById no encuentra nada, el flatMap no se ejecuta
        // y el Mono resultante estará vacío (lo cual es correcto).
        .flatMap(actual -> {
            // Si el correo no cambió, actualizar directamente
            if (actual.getCorreo().equals(cambios.getCorreo())) {
                actual.setNombre(cambios.getNombre());
                actual.setApellido(cambios.getApellido());
                return repo.save(actual);
            }

            // Si cambió, verificar si otro estudiante lo tiene
            return repo.findByCorreo(cambios.getCorreo())
                .flatMap(existente -> {
                    // Si el correo pertenece a otro estudiante, error
                    if (!existente.getId().equals(id)) {
                        // Esto sí es un 400 Bad Request
                        return Mono.error(new IllegalArgumentException("El correo ya está registrado"));
                    }
                    return Mono.just(actual);
                })
                .switchIfEmpty(Mono.just(actual))
                .flatMap(act -> {
                    act.setNombre(cambios.getNombre());
                    act.setApellido(cambios.getApellido());
                    act.setCorreo(cambios.getCorreo());
                    return repo.save(act);
                });
        });
}

public Mono<Double> calcularPromedio(Long estudianteId) {
        // 1. Verificar que el estudiante exista
        return repo.existsById(estudianteId)
            .flatMap(existe -> {
                if (!existe) {
                    // Lanzamos error para que el controlador lo convierta en 404
                    return Mono.error(new IllegalArgumentException("Estudiante no existe"));
                }

                // 2. Si existe, buscar todas sus notas
                return repoNota.findAllByEstudianteId(estudianteId)
                    .collectList() // Convertir el Flux<Nota> en un Mono<List<Nota>>
                    .map(notas -> {
                        if (notas.isEmpty()) {
                            return 0.0; // Si no tiene notas, el promedio es 0
                        }

                        // 3. Calcular el promedio ponderado
                        double sumaPonderada = notas.stream()
                            .mapToDouble(nota -> nota.getValor() * nota.getPorcentaje())
                            .sum();
                        
                        double sumaPorcentajes = notas.stream()
                            .mapToDouble(Nota::getPorcentaje)
                            .sum();

                        if (sumaPorcentajes == 0) {
                            return 0.0; // Evitar división por cero
                        }

                        // Devuelve el promedio (ej: (4.5*30 + 3.0*70) / (30 + 70) )
                        // Dividimos sumaPonderada por 100 si los porcentajes suman 100
                        // o por sumaPorcentajes si son parciales.
                        // Asumamos que la suma de porcentajes puede no ser 100.
                        return sumaPonderada / sumaPorcentajes;
                    });
            });
    }



    public Mono<Void> eliminar(Long id) {
        return repo.existsById(id)
                .flatMap(existe -> existe
                        ? repo.deleteById(id)
                        : Mono.error(new IllegalArgumentException("El estudiante no existe")));
    }
}