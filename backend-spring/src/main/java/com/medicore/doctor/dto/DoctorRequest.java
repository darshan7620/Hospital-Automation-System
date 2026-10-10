package com.medicore.doctor.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record DoctorRequest(
        @NotBlank(message = "fullName is required")
        @Size(max = 150, message = "fullName must be at most 150 characters")
        String fullName,

        @NotBlank(message = "specialization is required")
        @Size(max = 100, message = "specialization must be at most 100 characters")
        String specialization,

        @Size(max = 30, message = "phone must be at most 30 characters")
        String phone,

        @Email(message = "email must be a valid address")
        @Size(max = 150, message = "email must be at most 150 characters")
        String email,

        @NotNull(message = "consultationFee is required")
        @DecimalMin(value = "0.00", message = "consultationFee must not be negative")
        @Digits(integer = 8, fraction = 2, message = "consultationFee must have at most 8 integer and 2 fraction digits")
        BigDecimal consultationFee,

        @NotNull(message = "departmentId is required")
        Long departmentId) {
}
