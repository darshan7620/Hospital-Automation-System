package com.medicore.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name must be at most 100 characters")
        String name,

        @Size(max = 255, message = "description must be at most 255 characters")
        String description,

        @Size(max = 150, message = "location must be at most 150 characters")
        String location) {
}
