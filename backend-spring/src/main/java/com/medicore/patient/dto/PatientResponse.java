package com.medicore.patient.dto;

import java.time.Instant;
import java.time.LocalDate;

public record PatientResponse(
        Long id,
        String patientNumber,
        String fullName,
        LocalDate dateOfBirth,
        String phone,
        String address,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
}
