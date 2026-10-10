package com.medicore.department;

import com.medicore.common.dto.PageResponse;
import com.medicore.common.exception.ConflictException;
import com.medicore.common.exception.ResourceNotFoundException;
import com.medicore.department.dto.DepartmentRequest;
import com.medicore.department.dto.DepartmentResponse;
import com.medicore.doctor.DoctorRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;

    public DepartmentService(DepartmentRepository departmentRepository, DoctorRepository doctorRepository) {
        this.departmentRepository = departmentRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        String name = normalize(request.name());
        if (departmentRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("A department named '%s' already exists.".formatted(name));
        }
        Department department = new Department();
        department.setName(name);
        department.setDescription(normalizeNullable(request.description()));
        department.setLocation(normalizeNullable(request.location()));
        department.setActive(true);
        return DepartmentMapper.toResponse(departmentRepository.save(department));
    }

    public PageResponse<DepartmentResponse> list(String name, Boolean active, Pageable pageable) {
        String nameFilter = (name == null || name.isBlank()) ? null : name.trim();
        return PageResponse.from(
                departmentRepository.search(nameFilter, active, pageable).map(DepartmentMapper::toResponse));
    }

    public DepartmentResponse getById(Long id) {
        return DepartmentMapper.toResponse(getEntity(id));
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = getEntity(id);
        String name = normalize(request.name());
        if (departmentRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("A department named '%s' already exists.".formatted(name));
        }
        department.setName(name);
        department.setDescription(normalizeNullable(request.description()));
        department.setLocation(normalizeNullable(request.location()));
        return DepartmentMapper.toResponse(departmentRepository.save(department));
    }

    @Transactional
    public void deactivate(Long id) {
        Department department = getEntity(id);
        long activeDoctors = doctorRepository.countByDepartmentIdAndActiveTrue(id);
        if (activeDoctors > 0) {
            throw new ConflictException(
                    "Department with id %d has %d active doctor(s) and cannot be deactivated."
                            .formatted(id, activeDoctors));
        }
        department.setActive(false);
        departmentRepository.save(department);
    }

    @Transactional
    public DepartmentResponse activate(Long id) {
        Department department = getEntity(id);
        department.setActive(true);
        return DepartmentMapper.toResponse(departmentRepository.save(department));
    }

    private Department getEntity(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department with id %d was not found.".formatted(id)));
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
}
