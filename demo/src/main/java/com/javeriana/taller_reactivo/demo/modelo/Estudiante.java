package com.javeriana.taller_reactivo.demo.modelo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("estudiantes")
public class Estudiante {
    @Id
    private Long id;

    @NotBlank
    private String nombre;
    
    private String apellido;

    @Email
    @NotBlank
    private String correo;
}
