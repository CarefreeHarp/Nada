package com.javeriana.taller_reactivo.demo.Servicio;

import com.javeriana.taller_reactivo.demo.modelo.Materia;
import com.javeriana.taller_reactivo.demo.repositorio.RepositorioMateria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Transactional
public class ServicioMateria {

    private final RepositorioMateria repo;

    public ServicioMateria(RepositorioMateria repo) {
        this.repo = repo;
    }

    public Flux<Materia> listar() {
        return repo.findAll();
    }

    public Mono<Materia> porId(Long id) {
        return repo.findById(id);
    }

    public Mono<Materia> crear(Materia m) {
        if (m.getCreditos() <= 0) {
            return Mono.error(new IllegalArgumentException("Los créditos deben ser positivos"));
        }
        // nombre único
        return repo.existsByNombre(m.getNombre())
                .flatMap(existe -> existe
                        ? Mono.error(new IllegalArgumentException("El nombre de la materia ya existe"))
                        : repo.save(m));
    }

    public Mono<Materia> actualizar(Long id, Materia cambios) {
        if (cambios.getCreditos() <= 0) {
            return Mono.error(new IllegalArgumentException("Los créditos deben ser positivos"));
        }
        return repo.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Materia no existe")))
                .flatMap(actual -> {
                    actual.setNombre(cambios.getNombre());
                    actual.setCreditos(cambios.getCreditos());
                    return repo.findByNombre(cambios.getNombre())
                            .filter(m -> m.getId().equals(id))
                            .switchIfEmpty(
                                repo.existsByNombre(cambios.getNombre())
                                   .flatMap(existe -> existe
                                           ? Mono.error(new IllegalArgumentException("El nombre de la materia ya existe"))
                                           : Mono.just(actual))
                            )
                            .then(repo.save(actual));
                });
    }

    public Mono<Void> eliminar(Long id) {
        return repo.deleteById(id);
    }
}
