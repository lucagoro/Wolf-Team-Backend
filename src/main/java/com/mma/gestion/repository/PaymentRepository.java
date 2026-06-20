package com.mma.gestion.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mma.gestion.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findByStudentId(Long studentId);
    
    Optional<Payment> findTopByStudentIdOrderByDueDateDesc(Long studentId);

    boolean existsByStudentId(Long studentId);

    List<Payment> findByStudentIdOrderByPaymentDateDesc(Long studentId);

    // Consultas optimizadas para evitar N+1
    @Query("SELECT p.student.id, COUNT(p) FROM Payment p GROUP BY p.student.id")
    List<Object[]> countPaymentsByStudent();

    @Query("SELECT p.student.id, MAX(p.dueDate) FROM Payment p GROUP BY p.student.id")
    List<Object[]> findMaxDueDateByStudent();

    @Query("SELECT p.student.id, SUM(p.amount) FROM Payment p WHERE p.paymentDate >= :startOfMonth AND p.paymentDate < :endOfMonth GROUP BY p.student.id")
    List<Object[]> sumPaymentsByStudentInMonth(@Param("startOfMonth") LocalDate startOfMonth, @Param("endOfMonth") LocalDate endOfMonth);
}
