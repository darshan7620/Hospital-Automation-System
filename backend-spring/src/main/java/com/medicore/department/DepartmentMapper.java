package com.medicore.department;

import com.medicore.department.dto.DepartmentResponse;

final class DepartmentMapper {

    private DepartmentMapper() {
    }

    static DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getDescription(),
                department.getLocation(),
                department.isActive(),
                department.getCreatedAt(),
                department.getUpdatedAt());
    }
}
