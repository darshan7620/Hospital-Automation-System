package com.medicore.appointment.dto;

import com.medicore.appointment.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record AppointmentUpdateRequest(
        @NotNull(message = "patientId is required")
        Long patientId,

        @NotNull(message = "doctorId is required")
        Long doctorId,

        @NotNull(message = "startTime is required")
        Instant startTime,

        @NotNull(message = "endTime is required")
        Instant endTime,

        @Size(max = 500, message = "reason must be at most 500 characters")
        String reason,

        AppointmentStatus status) {
}
