package com.medicore.department.dto;

import java.time.Instant;

public record DepartmentResponse(
        Long id,
        String name,
        String description,
        String location,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
}
