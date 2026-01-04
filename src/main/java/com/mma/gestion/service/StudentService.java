package com.mma.gestion.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mma.gestion.StudentStatus;
import com.mma.gestion.dto.StudentDTO;
import com.mma.gestion.dto.StudentSummaryDTO;
import com.mma.gestion.entity.Payment;
import com.mma.gestion.entity.Student;
import com.mma.gestion.exception.PhoneAlreadyExistsException;
import com.mma.gestion.exception.StudentHasPaymentsException;
import com.mma.gestion.exception.StudentNotFoundException;
import com.mma.gestion.repository.PaymentRepository;
import com.mma.gestion.repository.StudentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final PaymentRepository paymentRepository;

    public StudentDTO getStudentById(Long id) {
        return studentRepository.findById(id)
                .map(student -> new StudentDTO(
                        student.getId(),
                        student.getName(),
                        student.getSurname(),
                        student.getPhone(),
                        calculateStatus(student)))
                .orElseThrow(() -> new StudentNotFoundException(id));
    }

    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAllByOrderBySurnameAsc().stream()     // stream convierte una colección en un stream (secuencia de elementos). Permite operaciones funcionales
                .map(student -> new StudentDTO(         // Conversión de entidad a DTO
                        student.getId(),
                        student.getName(),
                        student.getSurname(),
                        student.getPhone(),
                        calculateStatus(student)))
                .toList();                              // Convierte el stream de vuelta a una lista  
    }

    public StudentDTO createStudent(StudentDTO dto) {
        if (studentRepository.existsByPhone(dto.getPhone())) {
            throw new PhoneAlreadyExistsException(dto.getPhone());
        }
        Student student = new Student();
        student.setName(dto.getName());
        student.setSurname(dto.getSurname());
        student.setPhone(dto.getPhone());

        Student savedStudent = studentRepository.save(student);

        return new StudentDTO(
                savedStudent.getId(),
                savedStudent.getName(),
                savedStudent.getSurname(),
                savedStudent.getPhone(),
                calculateStatus(savedStudent));
}


   public StudentDTO updateStudent(Long id, StudentDTO dto) {
    Student student = studentRepository.findById(id)
            .orElseThrow(() -> new StudentNotFoundException(id));

    if (!student.getPhone().equals(dto.getPhone())
            && studentRepository.existsByPhone(dto.getPhone())) {
        throw new RuntimeException("Phone already registered");
    }

    student.setName(dto.getName());
    student.setSurname(dto.getSurname());
    student.setPhone(dto.getPhone());

    Student updatedStudent = studentRepository.save(student);

    return new StudentDTO(
            updatedStudent.getId(),
            updatedStudent.getName(),
            updatedStudent.getSurname(),
            updatedStudent.getPhone(),
            calculateStatus(updatedStudent)
    );
}

    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));

        if (paymentRepository.existsByStudentId(id)) {
            throw new StudentHasPaymentsException(id);
        }
        studentRepository.delete(student);
    }

    private StudentStatus calculateStatus(Student student) {
        Optional<Payment> lastPayment = paymentRepository.findTopByStudentIdOrderByDueDateDesc(student.getId());
        if (lastPayment.isEmpty())
            return StudentStatus.SIN_PAGOS;

        if (lastPayment.get().getDueDate().isBefore(LocalDate.now()))
            return StudentStatus.VENCIDO;
        else
            return StudentStatus.AL_DIA;

    }

    public List<StudentDTO> getStudentsByStatus(StudentStatus status) {

    return studentRepository.findAllByOrderBySurnameAsc().stream()
            .filter(student -> calculateStatus(student) == status)
            .map(student -> new StudentDTO(
                    student.getId(),
                    student.getName(),
                    student.getSurname(),
                    student.getPhone(),
                    status
            ))
            .toList();
    }

    // Mas adelante mejorar con una consulta personalizada en el repositorio
    public StudentSummaryDTO getStudentsSummary() {

    List<Student> students = studentRepository.findAll();

    long total = students.size();
    long alDia = 0;
    long vencidos = 0;
    long sinPagos = 0;
    BigDecimal totalMes = BigDecimal.ZERO;

    LocalDate today = LocalDate.now();

    YearMonth currentMonth = YearMonth.now();

    for (Student student : students) {

        Optional<Payment> lastPayment = paymentRepository.findTopByStudentIdOrderByDueDateDesc(student.getId());

        if (lastPayment.isEmpty()) {
            sinPagos++;
            continue;
        }

        if (lastPayment.get().getDueDate().isBefore(today)) {
            vencidos++;
        } else {
            alDia++;
        }

          if (YearMonth.from(lastPayment.get().getPaymentDate()).equals(currentMonth)) {
            totalMes = totalMes.add(lastPayment.get().getAmount());
        }
        
    }

    return new StudentSummaryDTO(total, alDia, vencidos, sinPagos, totalMes);
}

}
