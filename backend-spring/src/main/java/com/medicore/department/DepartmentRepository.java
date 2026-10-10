package com.medicore.department;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    long countByActiveTrue();

    @Query("""
            select d from Department d
            where (:name is null or lower(d.name) like lower(concat('%', :name, '%')))
              and (:active is null or d.active = :active)
            """)
    Page<Department> search(@Param("name") String name,
                            @Param("active") Boolean active,
                            Pageable pageable);
}
