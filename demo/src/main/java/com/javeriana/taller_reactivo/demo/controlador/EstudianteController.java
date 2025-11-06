package com.javeriana.taller_reactivo.demo.controlador;

import com.javeriana.taller_reactivo.demo.modelo.Estudiante;
import com.javeriana.taller_reactivo.demo.servicio.ServicioEstudiante;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/estudiantes")
public class EstudianteController {

    private final ServicioEstudiante servicio;

    public EstudianteController(ServicioEstudiante servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public Flux<Estudiante> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Estudiante>> porId(@PathVariable Long id) {
        return servicio.porId(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Estudiante>> crear(@Valid @RequestBody Mono<Estudiante> estudianteMono) {
        return estudianteMono
                .flatMap(servicio::crear)
                .map(e -> ResponseEntity.status(201).body(e));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Estudiante>> actualizar(@PathVariable Long id, @Valid @RequestBody Mono<Estudiante> estudianteMono) {
        return estudianteMono
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
                .flatMap(e -> servicio.eliminar(id).thenReturn(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
