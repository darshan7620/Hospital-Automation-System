package com.medicore.dashboard;

import com.medicore.appointment.Appointment;
import com.medicore.dashboard.dto.DashboardAppointmentResponse;
import com.medicore.department.Department;
import com.medicore.doctor.Doctor;
import com.medicore.patient.Patient;

final class DashboardMapper {

    private DashboardMapper() {
    }

    static DashboardAppointmentResponse toAppointment(Appointment appointment) {
        Patient patient = appointment.getPatient();
        Doctor doctor = appointment.getDoctor();
        Department department = doctor == null ? null : doctor.getDepartment();
        return new DashboardAppointmentResponse(
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
                appointment.getStatus());
    }
}
