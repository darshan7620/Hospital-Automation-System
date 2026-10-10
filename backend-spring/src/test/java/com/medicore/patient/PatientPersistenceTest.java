package com.medicore.patient;

import com.medicore.patient.dto.PatientRequest;
import com.medicore.patient.dto.PatientResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class PatientPersistenceTest {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PatientService patientService;

    private static PatientRequest request(String fullName) {
        return new PatientRequest(fullName, LocalDate.of(1990, 1, 1), "+1-555-0100", "123 Main St");
    }

    @Test
    void savesPatient_withGeneratedNumber_andTimestamps() {
        PatientResponse response = patientService.create(request("John Doe"));

        assertThat(response.id()).isNotNull();
        assertThat(response.patientNumber()).matches("PAT-\\d{6,}");
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();
    }

    @Test
    void generatesDistinctPatientNumbers_forMultiplePatients() {
        PatientResponse first = patientService.create(request("John Doe"));
        PatientResponse second = patientService.create(request("Jane Roe"));
        PatientResponse third = patientService.create(request("Sam Poe"));

        List<String> numbers = List.of(first.patientNumber(), second.patientNumber(), third.patientNumber());
        assertThat(numbers).doesNotHaveDuplicates();
    }

    @Test
    void rejectsDuplicatePatientNumber_atDatabaseLevel() {
        Patient first = new Patient();
        first.setPatientNumber("PAT-999999");
        first.setFullName("John Doe");
        first.setDateOfBirth(LocalDate.of(1990, 1, 1));
        first.setPhone("+1-555-0100");
        first.setActive(true);
        patientRepository.saveAndFlush(first);

        Patient duplicate = new Patient();
        duplicate.setPatientNumber("PAT-999999");
        duplicate.setFullName("Jane Roe");
        duplicate.setDateOfBirth(LocalDate.of(1991, 1, 1));
        duplicate.setPhone("+1-555-0101");
        duplicate.setActive(true);

        assertThatThrownBy(() -> patientRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
