package com.medicore.patient.dto;

public record PatientSummaryResponse(
        Long id,
        String patientNumber,
        String fullName,
        String phone,
        boolean active) {
}
