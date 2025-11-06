package com.javeriana.taller_reactivo.demo.servicio;

import com.javeriana.taller_reactivo.demo.modelo.Estudiante;
import com.javeriana.taller_reactivo.demo.repositorio.RepositorioEstudiante;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Transactional // transacciones reactivas
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
        // correo único (requisito)
        return repo.existsByCorreo(e.getCorreo())
                .flatMap(existe -> existe
                        ? Mono.error(new IllegalArgumentException("El correo ya está registrado"))
                        : repo.save(e));
    }

    public Mono<Estudiante> actualizar(Long id, Estudiante cambios) {
    return repo.findById(id)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Estudiante no existe")))
        .flatMap(actual -> 
            repo.findByCorreo(cambios.getCorreo())
                .flatMap(existente -> {
                    if (!existente.getId().equals(id)) {
                        return Mono.error(new IllegalArgumentException("El correo ya está registrado"));
                    }
                    return Mono.just(actual);
                })
                .defaultIfEmpty(actual) // si no hay nadie con ese correo -> OK
                .flatMap(act -> {
                    act.setNombre(cambios.getNombre());
                    act.setCorreo(cambios.getCorreo());
                    return repo.save(act);
                })
        );
}


    public Mono<Void> eliminar(Long id) {
        return repo.deleteById(id);
    }
}
