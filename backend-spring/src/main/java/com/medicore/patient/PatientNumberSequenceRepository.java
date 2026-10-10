package com.medicore.patient;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientNumberSequenceRepository extends JpaRepository<PatientNumberSequence, Long> {
}
