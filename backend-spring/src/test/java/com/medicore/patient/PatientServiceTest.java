package com.medicore.patient;

import com.medicore.common.dto.PageResponse;
import com.medicore.common.exception.ResourceNotFoundException;
import com.medicore.patient.dto.PatientRequest;
import com.medicore.patient.dto.PatientResponse;
import com.medicore.patient.dto.PatientSummaryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientNumberSequenceRepository patientNumberSequenceRepository;

    @InjectMocks
    private PatientService patientService;

    private static PatientRequest request() {
        return new PatientRequest("John Doe", LocalDate.of(1990, 1, 1), "+1-555-0100", "123 Main St");
    }

    private static Patient existingPatient() {
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setPatientNumber("PAT-000001");
        patient.setFullName("Old Name");
        patient.setDateOfBirth(LocalDate.of(1985, 5, 5));
        patient.setPhone("+1-555-0000");
        patient.setActive(true);
        return patient;
    }

    @Test
    void create_generatesPatientNumber_serverSide() {
        PatientNumberSequence sequence = new PatientNumberSequence();
        sequence.setId(1L);
        when(patientNumberSequenceRepository.save(any(PatientNumberSequence.class))).thenReturn(sequence);
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
            Patient patient = invocation.getArgument(0);
            patient.setId(42L);
            return patient;
        });

        PatientResponse response = patientService.create(request());

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.patientNumber()).isEqualTo("PAT-000001");
        assertThat(response.fullName()).isEqualTo("John Doe");
        assertThat(response.active()).isTrue();
    }

    @Test
    void create_persistsGeneratedNumber_notAnyClientValue() {
        PatientNumberSequence sequence = new PatientNumberSequence();
        sequence.setId(7L);
        when(patientNumberSequenceRepository.save(any(PatientNumberSequence.class))).thenReturn(sequence);
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        patientService.create(request());

        ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(captor.capture());
        assertThat(captor.getValue().getPatientNumber()).isEqualTo("PAT-000007");
    }

    @Test
    void getById_throwsNotFound_whenMissing() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_changesFields_butKeepsPatientNumber() {
        Patient existing = existingPatient();
        when(patientRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatientResponse response = patientService.update(1L, request());

        assertThat(response.patientNumber()).isEqualTo("PAT-000001");
        assertThat(response.fullName()).isEqualTo("John Doe");
        assertThat(response.phone()).isEqualTo("+1-555-0100");
    }

    @Test
    void deactivate_setsActiveFalse() {
        Patient existing = existingPatient();
        when(patientRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        patientService.deactivate(1L);

        ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    @Test
    void list_mapsEntitiesToSummaries() {
        Pageable pageable = PageRequest.of(0, 20);
        when(patientRepository.search(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(existingPatient()), pageable, 1));

        PageResponse<PatientSummaryResponse> response =
                patientService.list(null, null, null, pageable);

        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().getFirst().patientNumber()).isEqualTo("PAT-000001");
    }
}
