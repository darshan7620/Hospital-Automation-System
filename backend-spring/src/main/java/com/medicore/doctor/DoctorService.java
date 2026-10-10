package com.medicore.doctor;

import com.medicore.common.dto.PageResponse;
import com.medicore.common.exception.ConflictException;
import com.medicore.common.exception.ResourceNotFoundException;
import com.medicore.department.Department;
import com.medicore.department.DepartmentRepository;
import com.medicore.doctor.dto.DoctorRequest;
import com.medicore.doctor.dto.DoctorResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;

    public DoctorService(DoctorRepository doctorRepository, DepartmentRepository departmentRepository) {
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public DoctorResponse create(DoctorRequest request) {
        Department department = resolveAssignableDepartment(request.departmentId());
        String email = normalizeNullable(request.email());
        if (email != null && doctorRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("A doctor with email '%s' already exists.".formatted(email));
        }
        Doctor doctor = new Doctor();
        apply(doctor, request, department, email);
        doctor.setActive(true);
        return DoctorMapper.toResponse(doctorRepository.save(doctor));
    }

    public PageResponse<DoctorResponse> list(
            String name,
            String specialization,
            Long departmentId,
            Boolean active,
            Pageable pageable) {
        String nameFilter = normalizeFilter(name);
        String specializationFilter = normalizeFilter(specialization);
        return PageResponse.from(doctorRepository
                .search(nameFilter, specializationFilter, departmentId, active, pageable)
                .map(DoctorMapper::toResponse));
    }

    public DoctorResponse getById(Long id) {
        return DoctorMapper.toResponse(getEntity(id));
    }

    @Transactional
    public DoctorResponse update(Long id, DoctorRequest request) {
        Doctor doctor = getEntity(id);
        Department department = resolveAssignableDepartment(request.departmentId());
        String email = normalizeNullable(request.email());
        if (email != null && doctorRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ConflictException("A doctor with email '%s' already exists.".formatted(email));
        }
        apply(doctor, request, department, email);
        return DoctorMapper.toResponse(doctorRepository.save(doctor));
    }

    @Transactional
    public void deactivate(Long id) {
        Doctor doctor = getEntity(id);
        doctor.setActive(false);
        doctorRepository.save(doctor);
    }

    @Transactional
    public DoctorResponse activate(Long id) {
        Doctor doctor = getEntity(id);
        doctor.setActive(true);
        return DoctorMapper.toResponse(doctorRepository.save(doctor));
    }

    private void apply(Doctor doctor, DoctorRequest request, Department department, String email) {
        doctor.setFullName(normalize(request.fullName()));
        doctor.setSpecialization(normalize(request.specialization()));
        doctor.setPhone(normalizeNullable(request.phone()));
        doctor.setEmail(email);
        doctor.setConsultationFee(request.consultationFee());
        doctor.setDepartment(department);
    }

    private Department resolveAssignableDepartment(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department with id %d was not found.".formatted(departmentId)));
        if (!department.isActive()) {
            throw new ConflictException(
                    "Department with id %d is inactive and cannot be assigned to a doctor.".formatted(departmentId));
        }
        return department;
    }

    private Doctor getEntity(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor with id %d was not found.".formatted(id)));
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizeFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
