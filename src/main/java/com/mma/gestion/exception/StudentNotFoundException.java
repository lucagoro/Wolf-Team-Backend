package com.mma.gestion.exception;

public class StudentNotFoundException extends RuntimeException {

    public StudentNotFoundException(Long id) {
        super("Not found student with id: " + id);
    }
    
}
