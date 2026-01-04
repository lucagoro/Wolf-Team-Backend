package com.mma.gestion.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StudentSummaryDTO {
    private long total;
    private long alDia;
    private long vencidos;
    private long sinPagos;
    private BigDecimal totalMes;
}
