package com.mma.gestion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mma.gestion.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findByStudentId(Long studentId);
    
    Optional<Payment> findTopByStudentIdOrderByDueDateDesc(Long studentId);

    boolean existsByStudentId(Long studentId);

    List<Payment> findByStudentIdOrderByPaymentDateDesc(Long studentId);
}
