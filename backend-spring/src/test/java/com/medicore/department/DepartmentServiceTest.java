package com.medicore.department;

import com.medicore.common.exception.ConflictException;
import com.medicore.common.exception.ResourceNotFoundException;
import com.medicore.department.dto.DepartmentRequest;
import com.medicore.department.dto.DepartmentResponse;
import com.medicore.doctor.DoctorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DepartmentService departmentService;

    @Test
    void create_savesDepartment_withNormalizedName_whenUnique() {
        when(departmentRepository.existsByNameIgnoreCase("Cardiology")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> {
            Department department = invocation.getArgument(0);
            department.setId(1L);
            return department;
        });

        DepartmentResponse response = departmentService.create(
                new DepartmentRequest("  Cardiology  ", " Heart care ", " Block A "));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Cardiology");
        assertThat(response.description()).isEqualTo("Heart care");
        assertThat(response.location()).isEqualTo("Block A");
        assertThat(response.active()).isTrue();
    }

    @Test
    void create_throwsConflict_whenNameAlreadyExists() {
        when(departmentRepository.existsByNameIgnoreCase("Cardiology")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.create(new DepartmentRequest("Cardiology", null, null)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Cardiology");

        verify(departmentRepository, never()).save(any());
    }

    @Test
    void getById_throwsNotFound_whenMissing() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_throwsConflict_whenNewNameBelongsToAnotherDepartment() {
        Department existing = new Department();
        existing.setId(1L);
        existing.setName("Cardiology");
        existing.setActive(true);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departmentRepository.existsByNameIgnoreCaseAndIdNot("Neurology", 1L)).thenReturn(true);

        assertThatThrownBy(() -> departmentService.update(1L, new DepartmentRequest("Neurology", null, null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void deactivate_setsActiveFalse() {
        Department existing = new Department();
        existing.setId(1L);
        existing.setName("Cardiology");
        existing.setActive(true);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));

        departmentService.deactivate(1L);

        ArgumentCaptor<Department> captor = ArgumentCaptor.forClass(Department.class);
        verify(departmentRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    @Test
    void deactivate_throwsConflict_whenActiveDoctorsAreLinked() {
        Department existing = new Department();
        existing.setId(1L);
        existing.setName("Cardiology");
        existing.setActive(true);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(doctorRepository.countByDepartmentIdAndActiveTrue(1L)).thenReturn(2L);

        assertThatThrownBy(() -> departmentService.deactivate(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("active doctor");

        verify(departmentRepository, never()).save(any());
    }
}
