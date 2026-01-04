package com.mma.gestion.exception;

public class StudentHasPaymentsException extends RuntimeException {
    
     public StudentHasPaymentsException(Long id) {
        super("No se puede eliminar el alumno con id " + id + " porque tiene pagos registrados");
    }
}
