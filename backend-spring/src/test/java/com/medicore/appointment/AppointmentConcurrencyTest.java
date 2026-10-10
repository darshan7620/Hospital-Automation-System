package com.medicore.appointment;

import com.medicore.appointment.dto.AppointmentRequest;
import com.medicore.common.exception.ConflictException;
import com.medicore.department.Department;
import com.medicore.department.DepartmentRepository;
import com.medicore.doctor.Doctor;
import com.medicore.doctor.DoctorRepository;
import com.medicore.patient.Patient;
import com.medicore.patient.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AppointmentConcurrencyTest {

    private static final Instant START = Instant.parse("2030-09-01T10:00:00Z");
    private static final Instant END = Instant.parse("2030-09-01T10:30:00Z");

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Test
    void onlyOneConcurrentBookingForTheSameSlotSucceeds() throws Exception {
        Long[] ids = transactionTemplate.execute(status -> {
            Department department = new Department();
            department.setName("Dept-" + UUID.randomUUID().toString().substring(0, 8));
            department.setActive(true);
            departmentRepository.saveAndFlush(department);

            Doctor doctor = new Doctor();
            doctor.setFullName("Dr. Concurrency");
            doctor.setSpecialization("General");
            doctor.setEmail("conc-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
            doctor.setConsultationFee(new BigDecimal("100.00"));
            doctor.setDepartment(department);
            doctor.setActive(true);
            doctorRepository.saveAndFlush(doctor);

            Patient patient = new Patient();
            patient.setPatientNumber("T-" + UUID.randomUUID().toString().substring(0, 16));
            patient.setFullName("Concurrent Patient");
            patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
            patient.setPhone("+1-555-0100");
            patient.setActive(true);
            patientRepository.saveAndFlush(patient);

            return new Long[]{department.getId(), doctor.getId(), patient.getId()};
        });

        Long departmentId = ids[0];
        Long doctorId = ids[1];
        Long patientId = ids[2];

        try {
            AppointmentRequest request = new AppointmentRequest(patientId, doctorId, START, END, "Concurrency check");
            ExecutorService pool = Executors.newFixedThreadPool(2);
            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch go = new CountDownLatch(1);

            Callable<Boolean> booking = () -> {
                ready.countDown();
                go.await();
                try {
                    appointmentService.create(request);
                    return Boolean.TRUE;
                } catch (ConflictException exception) {
                    return Boolean.FALSE;
                }
            };

            Future<Boolean> first = pool.submit(booking);
            Future<Boolean> second = pool.submit(booking);
            try {
                assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
                go.countDown();
                boolean firstResult = first.get(20, TimeUnit.SECONDS);
                boolean secondResult = second.get(20, TimeUnit.SECONDS);

                assertThat(List.of(firstResult, secondResult)).containsExactlyInAnyOrder(true, false);
                assertThat(appointmentRepository.countConflicts(
                        doctorId, Set.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED), START, END, null))
                        .isEqualTo(1);
            } finally {
                pool.shutdownNow();
            }
        } finally {
            transactionTemplate.executeWithoutResult(status -> {
                appointmentRepository.deleteByDoctorId(doctorId);
                doctorRepository.deleteById(doctorId);
                patientRepository.deleteById(patientId);
                departmentRepository.deleteById(departmentId);
            });
        }
    }
}
