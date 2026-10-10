package com.medicore.department;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class DepartmentPersistenceTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    void savesAndReadsDepartment_withPopulatedTimestamps() {
        Department department = new Department();
        department.setName("Cardiology");
        department.setActive(true);

        Department saved = departmentRepository.saveAndFlush(department);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(departmentRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void rejectsDuplicateDepartmentName_atDatabaseLevel() {
        Department first = new Department();
        first.setName("Neurology");
        first.setActive(true);
        departmentRepository.saveAndFlush(first);

        Department duplicate = new Department();
        duplicate.setName("Neurology");
        duplicate.setActive(true);

        assertThatThrownBy(() -> departmentRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
