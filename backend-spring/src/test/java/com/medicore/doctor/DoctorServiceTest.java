package com.medicore.doctor;

import com.medicore.common.exception.ConflictException;
import com.medicore.common.exception.ResourceNotFoundException;
import com.medicore.department.Department;
import com.medicore.department.DepartmentRepository;
import com.medicore.doctor.dto.DoctorRequest;
import com.medicore.doctor.dto.DoctorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DoctorService doctorService;

    private static Department department(Long id, boolean active) {
        Department department = new Department();
        department.setId(id);
        department.setName("Cardiology");
        department.setActive(active);
        return department;
    }

    private static DoctorRequest request(Long departmentId) {
        return new DoctorRequest(
                "Alice Smith",
                "Cardiologist",
                "+1-555-0100",
                "alice.smith@example.com",
                new BigDecimal("150.00"),
                departmentId);
    }

    @Test
    void create_savesDoctor_whenDepartmentActiveAndEmailUnique() {
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(department(10L, true)));
        when(doctorRepository.existsByEmailIgnoreCase("alice.smith@example.com")).thenReturn(false);
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> {
            Doctor doctor = invocation.getArgument(0);
            doctor.setId(1L);
            return doctor;
        });

        DoctorResponse response = doctorService.create(request(10L));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.fullName()).isEqualTo("Alice Smith");
        assertThat(response.departmentId()).isEqualTo(10L);
        assertThat(response.departmentName()).isEqualTo("Cardiology");
        assertThat(response.active()).isTrue();
    }

    @Test
    void create_throwsNotFound_whenDepartmentMissing() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.create(request(99L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(doctorRepository, never()).save(any());
    }

    @Test
    void create_throwsConflict_whenDepartmentInactive() {
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(department(10L, false)));

        assertThatThrownBy(() -> doctorService.create(request(10L)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("inactive");

        verify(doctorRepository, never()).save(any());
    }

    @Test
    void create_throwsConflict_whenEmailAlreadyExists() {
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(department(10L, true)));
        when(doctorRepository.existsByEmailIgnoreCase("alice.smith@example.com")).thenReturn(true);

        assertThatThrownBy(() -> doctorService.create(request(10L)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("email");

        verify(doctorRepository, never()).save(any());
    }

    @Test
    void getById_throwsNotFound_whenMissing() {
        when(doctorRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.getById(7L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("7");
    }

    @Test
    void update_appliesChanges_whenValid() {
        Doctor existing = new Doctor();
        existing.setId(1L);
        existing.setFullName("Old Name");
        existing.setDepartment(department(10L, true));
        existing.setActive(true);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departmentRepository.findById(20L)).thenReturn(Optional.of(department(20L, true)));
        when(doctorRepository.existsByEmailIgnoreCaseAndIdNot("alice.smith@example.com", 1L)).thenReturn(false);
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DoctorResponse response = doctorService.update(1L, request(20L));

        assertThat(response.fullName()).isEqualTo("Alice Smith");
        assertThat(response.departmentId()).isEqualTo(20L);
    }

    @Test
    void update_throwsConflict_whenTargetDepartmentInactive() {
        Doctor existing = new Doctor();
        existing.setId(1L);
        existing.setDepartment(department(10L, true));
        existing.setActive(true);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departmentRepository.findById(20L)).thenReturn(Optional.of(department(20L, false)));

        assertThatThrownBy(() -> doctorService.update(1L, request(20L)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void deactivate_setsActiveFalse() {
        Doctor existing = new Doctor();
        existing.setId(1L);
        existing.setDepartment(department(10L, true));
        existing.setActive(true);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        doctorService.deactivate(1L);

        ArgumentCaptor<Doctor> captor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }
}
