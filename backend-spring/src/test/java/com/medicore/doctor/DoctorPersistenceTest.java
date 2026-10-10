package com.medicore.doctor;

import com.medicore.department.Department;
import com.medicore.department.DepartmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class DoctorPersistenceTest {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department persistDepartment(String name) {
        Department department = new Department();
        department.setName(name);
        department.setActive(true);
        return departmentRepository.saveAndFlush(department);
    }

    private Doctor doctor(String email, Department department) {
        Doctor doctor = new Doctor();
        doctor.setFullName("Alice Smith");
        doctor.setSpecialization("Cardiologist");
        doctor.setEmail(email);
        doctor.setConsultationFee(new BigDecimal("150.00"));
        doctor.setDepartment(department);
        doctor.setActive(true);
        return doctor;
    }

    @Test
    void savesDoctor_withDepartmentRelationship_andTimestamps() {
        Department department = persistDepartment("Cardiology");

        Doctor saved = doctorRepository.saveAndFlush(doctor("alice.smith@example.com", department));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getDepartment().getId()).isEqualTo(department.getId());
        assertThat(saved.getDepartment().getName()).isEqualTo("Cardiology");
    }

    @Test
    void rejectsDuplicateEmail_atDatabaseLevel() {
        Department department = persistDepartment("Neurology");
        doctorRepository.saveAndFlush(doctor("dup@example.com", department));

        assertThatThrownBy(() -> doctorRepository.saveAndFlush(doctor("dup@example.com", department)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void foreignKeyPreventsDeletingDepartmentWithLinkedDoctor() {
        Department department = persistDepartment("Oncology");
        doctorRepository.saveAndFlush(doctor("onc@example.com", department));

        departmentRepository.delete(department);

        assertThatThrownBy(departmentRepository::flush)
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
