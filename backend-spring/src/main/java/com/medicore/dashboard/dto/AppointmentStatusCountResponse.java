package com.medicore.dashboard.dto;

import com.medicore.appointment.AppointmentStatus;

public record AppointmentStatusCountResponse(
        AppointmentStatus status,
        long count) {
}
