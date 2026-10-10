package com.medicore.appointment;

import com.medicore.appointment.dto.AppointmentResponse;
import com.medicore.department.Department;
import com.medicore.doctor.Doctor;
import com.medicore.patient.Patient;

final class AppointmentMapper {

    private AppointmentMapper() {
    }

    static AppointmentResponse toResponse(Appointment appointment) {
        Patient patient = appointment.getPatient();
        Doctor doctor = appointment.getDoctor();
        Department department = doctor == null ? null : doctor.getDepartment();
        return new AppointmentResponse(
                appointment.getId(),
                patient == null ? null : patient.getId(),
                patient == null ? null : patient.getPatientNumber(),
                patient == null ? null : patient.getFullName(),
                doctor == null ? null : doctor.getId(),
                doctor == null ? null : doctor.getFullName(),
                department == null ? null : department.getId(),
                department == null ? null : department.getName(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getReason(),
                appointment.getStatus(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt());
    }
}
