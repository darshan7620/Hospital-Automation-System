package com.medicore.appointment;

import com.medicore.common.dto.PageResponse;
import com.medicore.common.exception.BadRequestException;
import com.medicore.common.exception.ConflictException;
import com.medicore.common.exception.ResourceNotFoundException;
import com.medicore.department.Department;
import com.medicore.doctor.Doctor;
import com.medicore.doctor.DoctorRepository;
import com.medicore.patient.Patient;
import com.medicore.patient.PatientRepository;
import com.medicore.appointment.dto.AppointmentRequest;
import com.medicore.appointment.dto.AppointmentResponse;
import com.medicore.appointment.dto.AppointmentUpdateRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class AppointmentService {

    private static final Set<AppointmentStatus> ACTIVE_STATUSES =
            Set.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED);

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AppointmentResponse create(AppointmentRequest request) {
        validateTimeRange(request.startTime(), request.endTime());
        Patient patient = resolveActivePatient(request.patientId());
        Doctor doctor = resolveActiveDoctorForUpdate(request.doctorId());
        ensureNoConflict(doctor.getId(), request.startTime(), request.endTime(), null);

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(request.endTime());
        appointment.setReason(normalizeNullable(request.reason()));
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        return AppointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    public PageResponse<AppointmentResponse> list(
            LocalDate date,
            Long doctorId,
            Long patientId,
            AppointmentStatus status,
            Pageable pageable) {
        Instant from = date == null ? null : date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = date == null ? null : date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return PageResponse.from(appointmentRepository
                .search(from, to, doctorId, patientId, status, pageable)
                .map(AppointmentMapper::toResponse));
    }

    public AppointmentResponse getById(Long id) {
        return AppointmentMapper.toResponse(getEntity(id));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AppointmentResponse update(Long id, AppointmentUpdateRequest request) {
        Appointment appointment = getEntity(id);
        validateTimeRange(request.startTime(), request.endTime());

        AppointmentStatus current = appointment.getStatus();
        AppointmentStatus target = request.status() == null ? current : request.status();
        boolean rescheduled = hasScheduleChanged(appointment, request);

        if (current.isTerminal()) {
            if (target != current) {
                throw new ConflictException(
                        "Appointment in status %s cannot transition to %s.".formatted(current, target));
            }
            if (rescheduled) {
                throw new ConflictException("A %s appointment cannot be rescheduled.".formatted(current));
            }
        } else if (target != current && !current.canTransitionTo(target)) {
            throw new ConflictException(
                    "Invalid status transition from %s to %s.".formatted(current, target));
        }

        Patient patient = resolveActivePatient(request.patientId());
        Doctor doctor = resolveActiveDoctorForUpdate(request.doctorId());
        if (target.isActive()) {
            ensureNoConflict(doctor.getId(), request.startTime(), request.endTime(), appointment.getId());
        }

        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(request.endTime());
        appointment.setReason(normalizeNullable(request.reason()));
        appointment.setStatus(target);
        return AppointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse cancel(Long id) {
        Appointment appointment = getEntity(id);
        if (!appointment.getStatus().canTransitionTo(AppointmentStatus.CANCELLED)) {
            throw new ConflictException(
                    "Appointment in status %s cannot be cancelled.".formatted(appointment.getStatus()));
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return AppointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    private void ensureNoConflict(Long doctorId, Instant start, Instant end, Long excludeId) {
        long conflicts = appointmentRepository.countConflicts(doctorId, ACTIVE_STATUSES, start, end, excludeId);
        if (conflicts > 0) {
            throw new ConflictException(
                    "The doctor already has an active appointment overlapping the requested time.");
        }
    }

    private Patient resolveActivePatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient with id %d was not found.".formatted(patientId)));
        if (!patient.isActive()) {
            throw new ConflictException("Patient with id %d is inactive.".formatted(patientId));
        }
        return patient;
    }

    private Doctor resolveActiveDoctorForUpdate(Long doctorId) {
        Doctor doctor = doctorRepository.findByIdForUpdate(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor with id %d was not found.".formatted(doctorId)));
        if (!doctor.isActive()) {
            throw new ConflictException("Doctor with id %d is inactive.".formatted(doctorId));
        }
        Department department = doctor.getDepartment();
        if (department == null || !department.isActive()) {
            throw new ConflictException(
                    "The department of doctor with id %d is inactive.".formatted(doctorId));
        }
        return doctor;
    }

    private Appointment getEntity(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment with id %d was not found.".formatted(id)));
    }

    private static void validateTimeRange(Instant start, Instant end) {
        if (start == null || end == null) {
            return;
        }
        if (!end.isAfter(start)) {
            throw new BadRequestException("endTime must be after startTime.");
        }
    }

    private static boolean hasScheduleChanged(Appointment appointment, AppointmentUpdateRequest request) {
        return !Objects.equals(request.startTime(), appointment.getStartTime())
                || !Objects.equals(request.endTime(), appointment.getEndTime())
                || !Objects.equals(request.patientId(), appointment.getPatient().getId())
                || !Objects.equals(request.doctorId(), appointment.getDoctor().getId());
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
