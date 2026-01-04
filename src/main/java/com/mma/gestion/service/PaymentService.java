package com.mma.gestion.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mma.gestion.dto.PaymentCreateDTO;
import com.mma.gestion.dto.PaymentDTO;
import com.mma.gestion.entity.Payment;
import com.mma.gestion.entity.Student;
import com.mma.gestion.exception.PaymentNotFoundException;
import com.mma.gestion.exception.StudentNotFoundException;
import com.mma.gestion.repository.PaymentRepository;
import com.mma.gestion.repository.StudentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final StudentRepository studentRepository;

    public List<PaymentDTO> getPaymentsByStudentId(Long studentId) {
         if (!studentRepository.existsById(studentId)) {
        throw new StudentNotFoundException(studentId);
        }

    return paymentRepository.findByStudentIdOrderByPaymentDateDesc(studentId)
            .stream()
            .map(payment -> new PaymentDTO(
                    payment.getId(),
                    payment.getAmount(),
                    payment.getPaymentDate(),
                    payment.getDueDate(),
                    payment.getPeriod(),
                    payment.getStudent().getId()
            ))
            .toList();
}

    public PaymentDTO getPaymentById(Long paymentId) {
    return paymentRepository.findById(paymentId)
            .map(payment -> new PaymentDTO(
                    payment.getId(),
                    payment.getAmount(),
                    payment.getPaymentDate(),
                    payment.getDueDate(),
                    payment.getPeriod(),
                    payment.getStudent().getId()
            ))
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));
}


    public List<PaymentDTO> getAllPayments() {
    return paymentRepository.findAll()
            .stream()
            .map(payment -> new PaymentDTO(
                    payment.getId(),
                    payment.getAmount(),
                    payment.getPaymentDate(),
                    payment.getDueDate(),
                    payment.getPeriod(),
                    payment.getStudent().getId()
            ))
            .toList();
}

    public PaymentDTO createPaymentForStudent(Long studentId, PaymentCreateDTO dto) {

    Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new StudentNotFoundException(studentId));

    Payment payment = new Payment();
    payment.setAmount(dto.getAmount());
    payment.setPaymentDate(dto.getPaymentDate());
    payment.setPeriod(dto.getPeriod());
    payment.setStudent(student);

    int days = payment.getStudent().getPlan().getDays();
    payment.setDueDate(dto.getPaymentDate().plusDays(days));

    Payment savedPayment = paymentRepository.save(payment);

    return new PaymentDTO(
            savedPayment.getId(),
            savedPayment.getAmount(),
            savedPayment.getPaymentDate(),
            savedPayment.getDueDate(),
            savedPayment.getPeriod(),
            student.getId()
    );
}


    public PaymentDTO updatePayment(Long paymentId, PaymentDTO dto) {
    Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));

    payment.setAmount(dto.getAmount());
    payment.setPeriod(dto.getPeriod());

    //  Si cambia la fecha de pago, recalculamos vencimiento
    if (!payment.getPaymentDate().equals(dto.getPaymentDate())) {
        payment.setPaymentDate(dto.getPaymentDate());

        int days = payment.getStudent().getPlan().getDays();
        payment.setDueDate(dto.getPaymentDate().plusDays(days));
    }

    Payment updatedPayment = paymentRepository.save(payment);

    return new PaymentDTO(
            updatedPayment.getId(),
            updatedPayment.getAmount(),
            updatedPayment.getPaymentDate(),
            updatedPayment.getDueDate(),
            updatedPayment.getPeriod(),
            updatedPayment.getStudent().getId()
    );
}

    public void deletePayment(Long paymentId) {
        paymentRepository.deleteById(paymentId);
    }
    
}
