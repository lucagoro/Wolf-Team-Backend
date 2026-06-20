package com.mma.gestion.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mma.gestion.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    
}
