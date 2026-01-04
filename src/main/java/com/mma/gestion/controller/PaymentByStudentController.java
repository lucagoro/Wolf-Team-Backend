package com.mma.gestion.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mma.gestion.dto.PaymentCreateDTO;
import com.mma.gestion.dto.PaymentDTO;
import com.mma.gestion.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class PaymentByStudentController {

    private final PaymentService paymentService;

    @PostMapping("/{studentId}/payments")
    public ResponseEntity<PaymentDTO> createPayment(
            @PathVariable Long studentId,
            @RequestBody PaymentCreateDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(paymentService.createPaymentForStudent(studentId, dto));
    }

    @GetMapping("/{studentId}/payments")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByStudent(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(paymentService.getPaymentsByStudentId(studentId));
    }
}

