package com.medicore.doctor;

import com.medicore.department.Department;
import com.medicore.doctor.dto.DoctorResponse;

final class DoctorMapper {

    private DoctorMapper() {
    }

    static DoctorResponse toResponse(Doctor doctor) {
        Department department = doctor.getDepartment();
        return new DoctorResponse(
                doctor.getId(),
                doctor.getFullName(),
                doctor.getSpecialization(),
                doctor.getPhone(),
                doctor.getEmail(),
                doctor.getConsultationFee(),
                department == null ? null : department.getId(),
                department == null ? null : department.getName(),
                doctor.isActive(),
                doctor.getCreatedAt(),
                doctor.getUpdatedAt());
    }
}
