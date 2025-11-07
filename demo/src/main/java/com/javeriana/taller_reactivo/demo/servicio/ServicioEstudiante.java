package com.javeriana.taller_reactivo.demo.servicio;

import com.javeriana.taller_reactivo.demo.modelo.Estudiante;
import com.javeriana.taller_reactivo.demo.repositorio.RepositorioEstudiante;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@Service
@Transactional
public class ServicioEstudiante {

    private final RepositorioEstudiante repo;

    public ServicioEstudiante(RepositorioEstudiante repo) {
        this.repo = repo;
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

public Mono<Estudiante> actualizar(Long id, Estudiante cambios) {
    return repo.findById(id)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Estudiante no existe")))
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
                        return Mono.error(new IllegalArgumentException("El correo ya está registrado"));
                    }
                    // Si pertenece al mismo, continuar normal
                    return Mono.just(actual);
                })
                // Si nadie más lo tiene, continuar normal
                .switchIfEmpty(Mono.just(actual))
                .flatMap(act -> {
                    act.setNombre(cambios.getNombre());
                    act.setApellido(cambios.getApellido());
                    act.setCorreo(cambios.getCorreo());
                    return repo.save(act);
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