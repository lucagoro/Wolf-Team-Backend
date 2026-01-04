package com.mma.gestion.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount; // Monto del pago

    private LocalDate paymentDate; // Fecha de pago

    private LocalDate dueDate; // Fecha de vencimiento 

    private String period; // Ej: 2025-03

    @ManyToOne(optional = false)
    private Student student; // Estudiante asociado al pago - muchos pagos pueden pertenecer a un estudiante

}
