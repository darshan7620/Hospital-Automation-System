package com.medicore.dashboard.dto;

import com.medicore.appointment.AppointmentStatus;

import java.time.Instant;

public record DashboardAppointmentResponse(
        Long id,
        Long patientId,
        String patientNumber,
        String patientName,
        Long doctorId,
        String doctorName,
        Long departmentId,
        String departmentName,
        Instant startTime,
        Instant endTime,
        AppointmentStatus status) {
}
