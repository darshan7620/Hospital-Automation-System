package com.medicore.appointment;

import com.medicore.appointment.dto.AppointmentRequest;
import com.medicore.appointment.dto.AppointmentResponse;
import com.medicore.appointment.dto.AppointmentUpdateRequest;
import com.medicore.common.exception.BadRequestException;
import com.medicore.common.exception.ConflictException;
import com.medicore.common.exception.ResourceNotFoundException;
import com.medicore.department.Department;
import com.medicore.doctor.Doctor;
import com.medicore.doctor.DoctorRepository;
import com.medicore.patient.Patient;
import com.medicore.patient.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    private static final Instant START = Instant.parse("2030-01-01T09:00:00Z");
    private static final Instant END = Instant.parse("2030-01-01T09:30:00Z");

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private static Patient patient(Long id, boolean active) {
        Patient patient = new Patient();
        patient.setId(id);
        patient.setPatientNumber("PAT-00000" + id);
        patient.setFullName("Test Patient");
        patient.setActive(active);
        return patient;
    }

    private static Department department(boolean active) {
        Department department = new Department();
        department.setId(3L);
        department.setName("Cardiology");
        department.setActive(active);
        return department;
    }

    private static Doctor doctor(Long id, boolean active, boolean departmentActive) {
        Doctor doctor = new Doctor();
        doctor.setId(id);
        doctor.setFullName("Dr. Test");
        doctor.setDepartment(department(departmentActive));
        doctor.setActive(active);
        return doctor;
    }

    private static Appointment appointment(Long id, AppointmentStatus status, Instant start, Instant end) {
        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setStatus(status);
        appointment.setPatient(patient(1L, true));
        appointment.setDoctor(doctor(2L, true, true));
        appointment.setStartTime(start);
        appointment.setEndTime(end);
        return appointment;
    }

    @Test
    void create_book_whenReferencesActiveAndSlotFree() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient(1L, true)));
        when(doctorRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(doctor(2L, true, true)));
        when(appointmentRepository.countConflicts(eq(2L), any(), eq(START), eq(END), isNull())).thenReturn(0L);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        AppointmentResponse response =
                appointmentService.create(new AppointmentRequest(1L, 2L, START, END, "Checkup"));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(response.doctorId()).isEqualTo(2L);
    }

    @Test
    void create_throwsBadRequest_whenEndNotAfterStart() {
        assertThatThrownBy(() ->
                appointmentService.create(new AppointmentRequest(1L, 2L, END, START, null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("endTime");
    }

    @Test
    void create_throwsNotFound_whenPatientMissing() {
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                appointmentService.create(new AppointmentRequest(1L, 2L, START, END, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_throwsConflict_whenPatientInactive() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient(1L, false)));

        assertThatThrownBy(() ->
                appointmentService.create(new AppointmentRequest(1L, 2L, START, END, null)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    void create_throwsNotFound_whenDoctorMissing() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient(1L, true)));
        when(doctorRepository.findByIdForUpdate(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                appointmentService.create(new AppointmentRequest(1L, 2L, START, END, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_throwsConflict_whenDoctorInactive() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient(1L, true)));
        when(doctorRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(doctor(2L, false, true)));

        assertThatThrownBy(() ->
                appointmentService.create(new AppointmentRequest(1L, 2L, START, END, null)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    void create_throwsConflict_whenDepartmentInactive() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient(1L, true)));
        when(doctorRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(doctor(2L, true, false)));

        assertThatThrownBy(() ->
                appointmentService.create(new AppointmentRequest(1L, 2L, START, END, null)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("department");
    }

    @Test
    void create_throwsConflict_whenOverlapping() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient(1L, true)));
        when(doctorRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(doctor(2L, true, true)));
        when(appointmentRepository.countConflicts(eq(2L), any(), eq(START), eq(END), isNull())).thenReturn(1L);

        assertThatThrownBy(() ->
                appointmentService.create(new AppointmentRequest(1L, 2L, START, END, null)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("overlapping");
    }

    @Test
    void update_throwsConflict_whenRescheduledToOverlappingSlot() {
        Instant newStart = START.plusSeconds(86400);
        Instant newEnd = newStart.plusSeconds(1800);
        when(appointmentRepository.findById(5L))
                .thenReturn(Optional.of(appointment(5L, AppointmentStatus.SCHEDULED, START, END)));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient(1L, true)));
        when(doctorRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(doctor(2L, true, true)));
        when(appointmentRepository.countConflicts(eq(2L), any(), eq(newStart), eq(newEnd), eq(5L))).thenReturn(1L);

        assertThatThrownBy(() -> appointmentService.update(5L,
                new AppointmentUpdateRequest(1L, 2L, newStart, newEnd, null, null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void update_throwsConflict_whenTransitioningFromTerminalState() {
        when(appointmentRepository.findById(5L))
                .thenReturn(Optional.of(appointment(5L, AppointmentStatus.COMPLETED, START, END)));

        assertThatThrownBy(() -> appointmentService.update(5L,
                new AppointmentUpdateRequest(1L, 2L, START, END, null, AppointmentStatus.SCHEDULED)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("cannot transition");
    }

    @Test
    void cancel_setsStatusCancelled() {
        when(appointmentRepository.findById(5L))
                .thenReturn(Optional.of(appointment(5L, AppointmentStatus.SCHEDULED, START, END)));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentResponse response = appointmentService.cancel(5L);

        assertThat(response.status()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    @Test
    void cancel_throwsConflict_whenAlreadyCancelled() {
        when(appointmentRepository.findById(5L))
                .thenReturn(Optional.of(appointment(5L, AppointmentStatus.CANCELLED, START, END)));

        assertThatThrownBy(() -> appointmentService.cancel(5L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("cannot be cancelled");
    }
}
