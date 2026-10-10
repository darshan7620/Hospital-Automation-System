package com.medicore.patient;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    boolean existsByPatientNumber(String patientNumber);

    long countByActiveTrue();

    @Query("""
            select p from Patient p
            where (:name is null or lower(p.fullName) like lower(concat('%', :name, '%')))
              and (:phone is null or p.phone like concat('%', :phone, '%'))
              and (:active is null or p.active = :active)
            """)
    Page<Patient> search(@Param("name") String name,
                         @Param("phone") String phone,
                         @Param("active") Boolean active,
                         Pageable pageable);
}
