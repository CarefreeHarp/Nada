package com.javeriana.taller_reactivo.demo.modelo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("materias")
public class Materia {
    @Id
    private Long id;

    @NotBlank
    private String nombre;

    @Min(1)
    private Integer creditos;
}
