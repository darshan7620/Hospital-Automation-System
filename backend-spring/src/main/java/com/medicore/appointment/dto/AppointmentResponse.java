package com.medicore.appointment.dto;

import com.medicore.appointment.AppointmentStatus;

import java.time.Instant;

public record AppointmentResponse(
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
        String reason,
        AppointmentStatus status,
        Instant createdAt,
        Instant updatedAt) {
}
