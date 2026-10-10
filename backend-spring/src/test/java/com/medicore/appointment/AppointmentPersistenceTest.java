package com.medicore.appointment;

import com.medicore.appointment.dto.AppointmentRequest;
import com.medicore.appointment.dto.AppointmentResponse;
import com.medicore.appointment.dto.AppointmentUpdateRequest;
import com.medicore.common.dto.PageResponse;
import com.medicore.common.exception.BadRequestException;
import com.medicore.common.exception.ConflictException;
import com.medicore.department.Department;
import com.medicore.department.DepartmentRepository;
import com.medicore.doctor.Doctor;
import com.medicore.doctor.DoctorRepository;
import com.medicore.patient.Patient;
import com.medicore.patient.PatientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class AppointmentPersistenceTest {

    private static final Instant DAY = Instant.parse("2030-06-01T00:00:00Z");

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

    private Department newDepartment(boolean active) {
        Department department = new Department();
        department.setName("Dept-" + UUID.randomUUID().toString().substring(0, 8));
        department.setActive(active);
        return departmentRepository.saveAndFlush(department);
    }

    private Doctor newDoctor(Department department, boolean active) {
        Doctor doctor = new Doctor();
        doctor.setFullName("Dr. Test");
        doctor.setSpecialization("General");
        doctor.setEmail("doc-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        doctor.setConsultationFee(new BigDecimal("100.00"));
        doctor.setDepartment(department);
        doctor.setActive(active);
        return doctorRepository.saveAndFlush(doctor);
    }

    private Patient newPatient(boolean active) {
        Patient patient = new Patient();
        patient.setPatientNumber("T-" + UUID.randomUUID().toString().substring(0, 16));
        patient.setFullName("Test Patient");
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient.setPhone("+1-555-0100");
        patient.setActive(active);
        return patientRepository.saveAndFlush(patient);
    }

    private static AppointmentRequest request(Long patientId, Long doctorId, Instant start, Instant end) {
        return new AppointmentRequest(patientId, doctorId, start, end, "Checkup");
    }

    @Test
    void booksAndRetrievesAppointment() {
        Doctor doctor = newDoctor(newDepartment(true), true);
        Patient patient = newPatient(true);

        AppointmentResponse created = appointmentService.create(
                request(patient.getId(), doctor.getId(), DAY.plusSeconds(9 * 3600), DAY.plusSeconds(9 * 3600 + 1800)));

        assertThat(created.id()).isNotNull();
        assertThat(created.status()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(created.createdAt()).isNotNull();

        AppointmentResponse fetched = appointmentService.getById(created.id());
        assertThat(fetched.patientId()).isEqualTo(patient.getId());
        assertThat(fetched.doctorId()).isEqualTo(doctor.getId());
    }

    @Test
    void rejectsOverlappingAppointmentForSameDoctor() {
        Doctor doctor = newDoctor(newDepartment(true), true);
        Patient patient = newPatient(true);

        appointmentService.create(request(patient.getId(), doctor.getId(), DAY.plusSeconds(3600), DAY.plusSeconds(5400)));

        assertThatThrownBy(() -> appointmentService.create(
                request(patient.getId(), doctor.getId(), DAY.plusSeconds(4500), DAY.plusSeconds(6300))))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void allowsBackToBackAppointments() {
        Doctor doctor = newDoctor(newDepartment(true), true);
        Patient patient = newPatient(true);

        AppointmentResponse first = appointmentService.create(
                request(patient.getId(), doctor.getId(), DAY.plusSeconds(3600), DAY.plusSeconds(5400)));
        AppointmentResponse second = appointmentService.create(
                request(patient.getId(), doctor.getId(), DAY.plusSeconds(5400), DAY.plusSeconds(7200)));

        assertThat(first.id()).isNotNull();
        assertThat(second.id()).isNotNull();
    }

    @Test
    void cancelledAppointmentDoesNotBlockSlot() {
        Doctor doctor = newDoctor(newDepartment(true), true);
        Patient patient = newPatient(true);
        Instant start = DAY.plusSeconds(3600);
        Instant end = DAY.plusSeconds(5400);

        AppointmentResponse first = appointmentService.create(request(patient.getId(), doctor.getId(), start, end));
        appointmentService.cancel(first.id());

        AppointmentResponse second =
                appointmentService.create(request(patient.getId(), doctor.getId(), start, end));

        assertThat(second.id()).isNotNull();
    }

    @Test
    void rejectsInactivePatient() {
        Doctor doctor = newDoctor(newDepartment(true), true);
        Patient patient = newPatient(false);

        assertThatThrownBy(() -> appointmentService.create(
                request(patient.getId(), doctor.getId(), DAY.plusSeconds(3600), DAY.plusSeconds(5400))))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void rejectsInactiveDoctor() {
        Doctor doctor = newDoctor(newDepartment(true), false);
        Patient patient = newPatient(true);

        assertThatThrownBy(() -> appointmentService.create(
                request(patient.getId(), doctor.getId(), DAY.plusSeconds(3600), DAY.plusSeconds(5400))))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void rejectsDoctorInInactiveDepartment() {
        Doctor doctor = newDoctor(newDepartment(false), true);
        Patient patient = newPatient(true);

        assertThatThrownBy(() -> appointmentService.create(
                request(patient.getId(), doctor.getId(), DAY.plusSeconds(3600), DAY.plusSeconds(5400))))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("department");
    }

    @Test
    void rejectsInvalidTimeRange() {
        Doctor doctor = newDoctor(newDepartment(true), true);
        Patient patient = newPatient(true);

        assertThatThrownBy(() -> appointmentService.create(
                request(patient.getId(), doctor.getId(), DAY.plusSeconds(5400), DAY.plusSeconds(3600))))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsRescheduleToOverlappingSlot() {
        Doctor doctor = newDoctor(newDepartment(true), true);
        Patient patient = newPatient(true);

        AppointmentResponse first = appointmentService.create(
                request(patient.getId(), doctor.getId(), DAY.plusSeconds(3600), DAY.plusSeconds(5400)));
        AppointmentResponse second = appointmentService.create(
                request(patient.getId(), doctor.getId(), DAY.plusSeconds(7200), DAY.plusSeconds(9000)));

        assertThatThrownBy(() -> appointmentService.update(second.id(), new AppointmentUpdateRequest(
                patient.getId(), doctor.getId(), DAY.plusSeconds(4500), DAY.plusSeconds(6300), null, null)))
                .isInstanceOf(ConflictException.class);

        assertThat(first.id()).isNotNull();
    }

    @Test
    void preventsTransitionFromTerminalState() {
        Doctor doctor = newDoctor(newDepartment(true), true);
        Patient patient = newPatient(true);
        Instant start = DAY.plusSeconds(3600);
        Instant end = DAY.plusSeconds(5400);

        AppointmentResponse created = appointmentService.create(request(patient.getId(), doctor.getId(), start, end));
        appointmentService.cancel(created.id());

        assertThatThrownBy(() -> appointmentService.update(created.id(), new AppointmentUpdateRequest(
                patient.getId(), doctor.getId(), start, end, null, AppointmentStatus.COMPLETED)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void filtersByDateDoctorAndStatusWithPagination() {
        Department department = newDepartment(true);
        Doctor doctorA = newDoctor(department, true);
        Doctor doctorB = newDoctor(department, true);
        Patient patient = newPatient(true);

        appointmentService.create(request(patient.getId(), doctorA.getId(), DAY.plusSeconds(3600), DAY.plusSeconds(5400)));
        appointmentService.create(request(patient.getId(), doctorA.getId(), DAY.plusSeconds(7200), DAY.plusSeconds(9000)));
        appointmentService.create(request(patient.getId(), doctorB.getId(), DAY.plusSeconds(3600), DAY.plusSeconds(5400)));

        PageResponse<AppointmentResponse> forDoctorA = appointmentService.list(
                DAY.atZone(java.time.ZoneOffset.UTC).toLocalDate(), doctorA.getId(), null, null, PageRequest.of(0, 10));

        assertThat(forDoctorA.totalElements()).isEqualTo(2);

        PageResponse<AppointmentResponse> aFirstPage = appointmentService.list(
                DAY.atZone(java.time.ZoneOffset.UTC).toLocalDate(), doctorA.getId(), null, null, PageRequest.of(0, 1));
        assertThat(aFirstPage.content()).hasSize(1);
        assertThat(aFirstPage.totalPages()).isEqualTo(2);
    }

    @Test
    void databaseRejectsEndNotAfterStart() {
        Doctor doctor = newDoctor(newDepartment(true), true);
        Patient patient = newPatient(true);

        Appointment invalid = new Appointment();
        invalid.setPatient(patient);
        invalid.setDoctor(doctor);
        invalid.setStartTime(DAY.plusSeconds(5400));
        invalid.setEndTime(DAY.plusSeconds(3600));
        invalid.setStatus(AppointmentStatus.SCHEDULED);

        assertThatThrownBy(() -> appointmentRepository.saveAndFlush(invalid))
                .isInstanceOf(JpaSystemException.class)
                .hasMessageContaining("chk_appointments_time");
    }
}
