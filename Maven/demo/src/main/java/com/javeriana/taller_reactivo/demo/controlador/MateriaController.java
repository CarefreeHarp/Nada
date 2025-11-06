package com.javeriana.taller_reactivo.demo.controlador;

import com.javeriana.taller_reactivo.demo.modelo.Materia;
import com.javeriana.taller_reactivo.demo.servicio.ServicioMateria;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/materias")
public class MateriaController {

    private final ServicioMateria servicio;

    public MateriaController(ServicioMateria servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public Flux<Materia> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Materia>> porId(@PathVariable Long id) {
        return servicio.porId(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Materia>> crear(@Valid @RequestBody Mono<Materia> materiaMono) {
        return materiaMono
                .flatMap(servicio::crear)
                .map(m -> ResponseEntity.status(201).body(m));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Materia>> actualizar(@PathVariable Long id, @Valid @RequestBody Mono<Materia> materiaMono) {
        return materiaMono
                .flatMap(cambios -> servicio.actualizar(id, cambios))
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalArgumentException.class, ex ->
                    Mono.just(ResponseEntity.badRequest().build())
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> eliminar(@PathVariable Long id) {
        return servicio.porId(id)
                .flatMap(m -> servicio.eliminar(id).thenReturn(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
