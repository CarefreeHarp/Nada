package com.javeriana.taller_reactivo.demo.controlador;

import com.javeriana.taller_reactivo.demo.modelo.Nota;
import com.javeriana.taller_reactivo.demo.servicio.ServicioNota;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/notas")
public class NotaController {

    private final ServicioNota servicio;

    public NotaController(ServicioNota servicio) {
        this.servicio = servicio;
    }

    // ----------------------------------------------------------------------
    // LISTAR (Estos ya estaban bien)
    // ----------------------------------------------------------------------

    @GetMapping
    public Flux<Nota> listar() {
        return servicio.listar();
    }

    @GetMapping("/estudiante/{estudianteId}")
    public Flux<Nota> listarPorEstudiante(@PathVariable Long estudianteId) {
        return servicio.listarPorEstudiante(estudianteId);
    }

    @GetMapping("/materia/{materiaId}")
    public Flux<Nota> listarPorMateria(@PathVariable Long materiaId) {
        return servicio.listarPorMateria(materiaId);
    }

    @GetMapping("/estudiante/{estudianteId}/materia/{materiaId}")
    public Flux<Nota> listarPorEstudianteMateria(@PathVariable Long estudianteId,
                                               @PathVariable Long materiaId) {
        return servicio.listarPorEstudianteMateria(estudianteId, materiaId);
    }

    // ----------------------------------------------------------------------
    // CREAR (Corregido)
    // ----------------------------------------------------------------------
    @PostMapping
    public Mono<ResponseEntity<?>> crear(@RequestBody Mono<Nota> notaMono) {
        return notaMono
            .flatMap(servicio::crear)
            // SOLUCIÓN: Añade .<ResponseEntity<?>> antes de .map
            .<ResponseEntity<?>>map(saved -> ResponseEntity
                .created(URI.create("/notas/" + saved.getId()))
                .body(saved))
            .onErrorResume(IllegalArgumentException.class, ex ->
                Mono.just(ResponseEntity
                    .badRequest()
                    .body(Map.of("error", ex.getMessage()))));
    }

    // ----------------------------------------------------------------------
    // ACTUALIZAR (Corregido)
    // ----------------------------------------------------------------------
    @PutMapping("/{id}")
    public Mono<ResponseEntity<?>> actualizar(@PathVariable Long id,
                                              @RequestBody Mono<Nota> notaMono) {
        return notaMono
            .flatMap(n -> servicio.actualizar(id, n))
            // SOLUCIÓN: Añade .<ResponseEntity<?>> antes de .map
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .onErrorResume(IllegalArgumentException.class, ex ->
                Mono.just(ResponseEntity
                    .badRequest()
                    .body(Map.of("error", ex.getMessage()))))
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // ----------------------------------------------------------------------
    // ELIMINAR (Corregido)
    // ----------------------------------------------------------------------
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<?>> eliminar(@PathVariable Long id) {
        return servicio.eliminar(id)
            // SOLUCIÓN: Añade .<ResponseEntity<?>> antes de .then
            .<ResponseEntity<?>>then(Mono.just(ResponseEntity.noContent().build()))
            .onErrorResume(IllegalArgumentException.class, ex ->
                Mono.just(ResponseEntity
                    .badRequest()
                    .body(Map.of("error", ex.getMessage()))));
    }

    // ----------------------------------------------------------------------
    // PROMEDIO PONDERADO (Corregido)
    // ----------------------------------------------------------------------
    @GetMapping("/estudiante/{estudianteId}/materia/{materiaId}/promedio")
    public Mono<ResponseEntity<?>> promedioPonderado(@PathVariable Long estudianteId,
                                                     @PathVariable Long materiaId) {
        return servicio.promedioPonderado(estudianteId, materiaId)
            // SOLUCIÓN: Añade .<ResponseEntity<?>> antes de .map
            .<ResponseEntity<?>>map(prom -> ResponseEntity.ok(Map.of("promedioPonderado", prom)))
            .switchIfEmpty(Mono.just(ResponseEntity
                .badRequest()
                .body(Map.of("error", "No existen notas para calcular el promedio"))))
            .onErrorResume(IllegalArgumentException.class, ex ->
                Mono.just(ResponseEntity
                    .badRequest()
                    .body(Map.of("error", ex.getMessage()))));
    }
}