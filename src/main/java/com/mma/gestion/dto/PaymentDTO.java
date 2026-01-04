package com.mma.gestion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private Long id;

    private BigDecimal amount;

    private LocalDate paymentDate;

    private LocalDate dueDate;

    private String period;

    private Long studentId;
}
