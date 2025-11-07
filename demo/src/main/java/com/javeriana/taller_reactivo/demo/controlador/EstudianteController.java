package com.javeriana.taller_reactivo.demo.controlador;

import com.javeriana.taller_reactivo.demo.modelo.Estudiante;
import com.javeriana.taller_reactivo.demo.servicio.ServicioEstudiante;

import jakarta.validation.Valid;
import org.springframework.context.support.DefaultMessageSourceResolvable; // <-- IMPORTAR
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException; // <-- IMPORTAR
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors; // <-- IMPORTAR

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
    public Mono<ResponseEntity<Estudiante>> crear(@Valid @RequestBody Estudiante estudiante) {
        // Esto ya estaba bien. Si la validación falla, el @ExceptionHandler de abajo lo atrapará.
        return servicio.crear(estudiante)
                .map(e -> ResponseEntity.status(201).body(e))
                // Manejamos el error "El correo ya existe" del servicio
                .onErrorResume(IllegalArgumentException.class, 
                    ex -> Mono.just(ResponseEntity.badRequest().body(null))); // Devuelve 400
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Estudiante>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Estudiante cambios) {
        return servicio.actualizar(id, cambios)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalArgumentException.class,
                    ex -> Mono.just(ResponseEntity.badRequest().build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/promedio")
    public Mono<ResponseEntity<Double>> getPromedio(@PathVariable Long id) {
        return servicio.calcularPromedio(id)
                .map(ResponseEntity::ok) // Si todo sale bien, devuelve 200 OK con el promedio
                .onErrorResume(IllegalArgumentException.class, // Si el servicio lanzó error (estudiante no existe)
                    ex -> Mono.just(ResponseEntity.notFound().build())); // Devuelve 404
    }


    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> eliminar(@PathVariable Long id) {
        return servicio.eliminar(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(IllegalArgumentException.class, ex ->
                    Mono.just(ResponseEntity.notFound().build())
                );
    }

    // --- MANEJADOR DE ERRORES DE VALIDACIÓN ---
    // ESTA ES LA PARTE QUE FALTABA
    // Atrapa los errores de @NotBlank, @Email, etc.
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<String>> handleBindException(WebExchangeBindException ex) {
        String errores = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("\n")); // Mensajes de error separados por línea

        // Devuelve un 400 Bad Request con los mensajes de error
        return Mono.just(ResponseEntity.badRequest().body("Error de validación: \n" + errores));
    }
}