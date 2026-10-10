package com.medicore.dashboard.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record DashboardSummaryResponse(
        LocalDate date,
        Instant generatedAt,
        long totalActivePatients,
        long totalActiveDoctors,
        long totalActiveDepartments,
        long todayAppointments,
        long upcomingAppointments,
        List<AppointmentStatusCountResponse> appointmentsByStatus) {
}
