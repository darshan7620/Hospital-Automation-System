package com.medicore.patient;

import com.medicore.common.dto.PageResponse;
import com.medicore.common.exception.ResourceNotFoundException;
import com.medicore.patient.dto.PatientRequest;
import com.medicore.patient.dto.PatientResponse;
import com.medicore.patient.dto.PatientSummaryResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PatientService {

    private static final String PATIENT_NUMBER_FORMAT = "PAT-%06d";

    private final PatientRepository patientRepository;
    private final PatientNumberSequenceRepository patientNumberSequenceRepository;

    public PatientService(PatientRepository patientRepository,
                          PatientNumberSequenceRepository patientNumberSequenceRepository) {
        this.patientRepository = patientRepository;
        this.patientNumberSequenceRepository = patientNumberSequenceRepository;
    }

    @Transactional
    public PatientResponse create(PatientRequest request) {
        Patient patient = new Patient();
        patient.setPatientNumber(generatePatientNumber());
        apply(patient, request);
        patient.setActive(true);
        return PatientMapper.toResponse(patientRepository.save(patient));
    }

    public PageResponse<PatientSummaryResponse> list(
            String name,
            String phone,
            Boolean active,
            Pageable pageable) {
        return PageResponse.from(patientRepository
                .search(normalizeFilter(name), normalizeFilter(phone), active, pageable)
                .map(PatientMapper::toSummary));
    }

    public PatientResponse getById(Long id) {
        return PatientMapper.toResponse(getEntity(id));
    }

    @Transactional
    public PatientResponse update(Long id, PatientRequest request) {
        Patient patient = getEntity(id);
        apply(patient, request);
        return PatientMapper.toResponse(patientRepository.save(patient));
    }

    @Transactional
    public void deactivate(Long id) {
        Patient patient = getEntity(id);
        patient.setActive(false);
        patientRepository.save(patient);
    }

    @Transactional
    public PatientResponse activate(Long id) {
        Patient patient = getEntity(id);
        patient.setActive(true);
        return PatientMapper.toResponse(patientRepository.save(patient));
    }

    private String generatePatientNumber() {
        PatientNumberSequence sequence = patientNumberSequenceRepository.save(new PatientNumberSequence());
        return PATIENT_NUMBER_FORMAT.formatted(sequence.getId());
    }

    private void apply(Patient patient, PatientRequest request) {
        patient.setFullName(normalize(request.fullName()));
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setPhone(normalize(request.phone()));
        patient.setAddress(normalizeNullable(request.address()));
    }

    private Patient getEntity(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient with id %d was not found.".formatted(id)));
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
