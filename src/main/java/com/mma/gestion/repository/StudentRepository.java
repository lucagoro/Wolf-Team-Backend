package com.mma.gestion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mma.gestion.entity.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    boolean existsByPhone(String phone);

    List<Student> findAllByOrderBySurnameAsc();

}
