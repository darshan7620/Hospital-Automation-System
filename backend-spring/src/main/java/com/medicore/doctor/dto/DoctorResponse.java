package com.medicore.doctor.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record DoctorResponse(
        Long id,
        String fullName,
        String specialization,
        String phone,
        String email,
        BigDecimal consultationFee,
        Long departmentId,
        String departmentName,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
}
