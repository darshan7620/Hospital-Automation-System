package com.medicore.doctor;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    long countByDepartmentIdAndActiveTrue(Long departmentId);

    long countByActiveTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Doctor d where d.id = :id")
    Optional<Doctor> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select d from Doctor d
            where (:name is null or lower(d.fullName) like lower(concat('%', :name, '%')))
              and (:specialization is null or lower(d.specialization) like lower(concat('%', :specialization, '%')))
              and (:departmentId is null or d.department.id = :departmentId)
              and (:active is null or d.active = :active)
            """)
    Page<Doctor> search(@Param("name") String name,
                        @Param("specialization") String specialization,
                        @Param("departmentId") Long departmentId,
                        @Param("active") Boolean active,
                        Pageable pageable);
}
