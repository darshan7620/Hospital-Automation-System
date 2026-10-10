package com.medicore.patient;

import com.medicore.patient.dto.PatientResponse;
import com.medicore.patient.dto.PatientSummaryResponse;

final class PatientMapper {

    private PatientMapper() {
    }

    static PatientResponse toResponse(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getPatientNumber(),
                patient.getFullName(),
                patient.getDateOfBirth(),
                patient.getPhone(),
                patient.getAddress(),
                patient.isActive(),
                patient.getCreatedAt(),
                patient.getUpdatedAt());
    }

    static PatientSummaryResponse toSummary(Patient patient) {
        return new PatientSummaryResponse(
                patient.getId(),
                patient.getPatientNumber(),
                patient.getFullName(),
                patient.getPhone(),
                patient.isActive());
    }
}
