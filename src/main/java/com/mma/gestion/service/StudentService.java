package com.mma.gestion.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        // Optimizado: obtener todos los últimos pagos en una sola consulta
        List<Object[]> maxDueDates = paymentRepository.findMaxDueDateByStudent();
        Map<Long, LocalDate> lastPaymentDates = maxDueDates.stream()
            .collect(Collectors.toMap(
                row -> (Long) row[0],
                row -> (LocalDate) row[1]
            ));

        LocalDate today = LocalDate.now();

        return studentRepository.findAllByOrderBySurnameAsc().stream()
                .map(student -> {
                    LocalDate lastDueDate = lastPaymentDates.get(student.getId());
                    StudentStatus status = calculateStatusFromDate(lastDueDate, today);
                    return new StudentDTO(
                        student.getId(),
                        student.getName(),
                        student.getSurname(),
                        student.getPhone(),
                        status);
                })
                .toList();
    }

    // Método auxiliar para calcular status sin consulta adicional
    private StudentStatus calculateStatusFromDate(LocalDate lastDueDate, LocalDate today) {
        if (lastDueDate == null) {
            return StudentStatus.SIN_PAGOS;
        }
        return lastDueDate.isBefore(today) ? StudentStatus.VENCIDO : StudentStatus.AL_DIA;
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
        // Optimizado: obtener todos los últimos pagos en una sola consulta
        List<Object[]> maxDueDates = paymentRepository.findMaxDueDateByStudent();
        Map<Long, LocalDate> lastPaymentDates = maxDueDates.stream()
            .collect(Collectors.toMap(
                row -> (Long) row[0],
                row -> (LocalDate) row[1]
            ));

        LocalDate today = LocalDate.now();

        return studentRepository.findAllByOrderBySurnameAsc().stream()
                .filter(student -> {
                    LocalDate lastDueDate = lastPaymentDates.get(student.getId());
                    StudentStatus studentStatus = calculateStatusFromDate(lastDueDate, today);
                    return studentStatus == status;
                })
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

    // Obtener todos los pagos agrupados en una sola consulta
    List<Object[]> maxDueDates = paymentRepository.findMaxDueDateByStudent();
    List<Object[]> monthlyPayments = paymentRepository.sumPaymentsByStudentInMonth(
        LocalDate.now().withDayOfMonth(1),
        LocalDate.now().withDayOfMonth(1).plusMonths(1)
    );

    // Crear mapas para acceso rápido O(1)
    Map<Long, LocalDate> lastPaymentDates = maxDueDates.stream()
        .collect(Collectors.toMap(
            row -> (Long) row[0],
            row -> (LocalDate) row[1]
        ));

    Map<Long, BigDecimal> monthlyAmounts = monthlyPayments.stream()
        .collect(Collectors.toMap(
            row -> (Long) row[0],
            row -> (BigDecimal) row[1]
        ));

    long total = students.size();
    long alDia = 0;
    long vencidos = 0;
    long sinPagos = 0;
    BigDecimal totalMes = BigDecimal.ZERO;

    LocalDate today = LocalDate.now();

    for (Student student : students) {
        LocalDate lastDueDate = lastPaymentDates.get(student.getId());

        if (lastDueDate == null) {
            sinPagos++;
            continue;
        }

        if (lastDueDate.isBefore(today)) {
            vencidos++;
        } else {
            alDia++;
        }

        BigDecimal amount = monthlyAmounts.get(student.getId());
        if (amount != null) {
            totalMes = totalMes.add(amount);
        }
    }

    return new StudentSummaryDTO(total, alDia, vencidos, sinPagos, totalMes);
}

    // Método con paginación
    public Page<StudentDTO> getStudentsPaginated(Pageable pageable, StudentStatus status) {
        // Obtener últimos pagos una sola vez
        List<Object[]> maxDueDates = paymentRepository.findMaxDueDateByStudent();
        Map<Long, LocalDate> lastPaymentDates = maxDueDates.stream()
            .collect(Collectors.toMap(
                row -> (Long) row[0],
                row -> (LocalDate) row[1]
            ));

        LocalDate today = LocalDate.now();

        Page<Student> studentsPage = studentRepository.findAll(pageable);

        Page<StudentDTO> dtoPage = studentsPage.map(student -> {
            LocalDate lastDueDate = lastPaymentDates.get(student.getId());
            StudentStatus studentStatus = calculateStatusFromDate(lastDueDate, today);
            return new StudentDTO(
                student.getId(),
                student.getName(),
                student.getSurname(),
                student.getPhone(),
                status != null ? status : studentStatus
            );
        });

        // Filtrar por status si se requiere
        if (status != null) {
            List<StudentDTO> filtered = dtoPage.getContent().stream()
                .filter(dto -> dto.getStatus() == status)
                .toList();
            return new org.springframework.data.domain.PageImpl<>(
                filtered, pageable, studentsPage.getTotalElements());
        }

        return dtoPage;
    }

}
