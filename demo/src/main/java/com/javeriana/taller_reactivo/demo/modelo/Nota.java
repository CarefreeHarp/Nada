package com.javeriana.taller_reactivo.demo.modelo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("notas")
public class Nota {
    @Id
    private Long id;

    @NotNull
    private Long estudianteId;

    @NotNull
    private Long materiaId;

    private String observacion;
    
    @DecimalMin("0.0")
    @DecimalMax("5.0")
    private BigDecimal valor;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private double porcentaje;

}
