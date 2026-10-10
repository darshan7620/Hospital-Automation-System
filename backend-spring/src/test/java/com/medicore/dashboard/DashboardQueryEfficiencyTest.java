package com.medicore.dashboard;

import com.medicore.appointment.Appointment;
import com.medicore.appointment.AppointmentRepository;
import com.medicore.appointment.AppointmentStatus;
import com.medicore.common.dto.PageResponse;
import com.medicore.dashboard.dto.DashboardAppointmentResponse;
import com.medicore.department.Department;
import com.medicore.department.DepartmentRepository;
import com.medicore.doctor.Doctor;
import com.medicore.doctor.DoctorRepository;
import com.medicore.patient.Patient;
import com.medicore.patient.PatientRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Transactional
class DashboardQueryEfficiencyTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void todayAppointments_doNotTriggerNPlusOneQueries() {
        Instant dayStart = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant();
        List<String> departmentNames = IntStream.range(0, 5)
                .mapToObj(index -> "Efficient-" + index)
                .toList();

        List<Appointment> appointments = new ArrayList<>();
        for (int index = 0; index < departmentNames.size(); index++) {
            Department department = new Department();
            department.setName(departmentNames.get(index));
            department.setActive(true);
            departmentRepository.saveAndFlush(department);

            Doctor doctor = new Doctor();
            doctor.setFullName("Doctor-" + index);
            doctor.setSpecialization("General");
            doctor.setEmail("efficient-" + index + "@example.com");
            doctor.setConsultationFee(new BigDecimal("100.00"));
            doctor.setDepartment(department);
            doctor.setActive(true);
            doctorRepository.saveAndFlush(doctor);

            Patient patient = new Patient();
            patient.setPatientNumber("E-" + index);
            patient.setFullName("Patient-" + index);
            patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
            patient.setPhone("+1-555-0100");
            patient.setActive(true);
            patientRepository.saveAndFlush(patient);

            Instant start = dayStart.plusSeconds(3600L * (index + 1));
            Appointment appointment = new Appointment();
            appointment.setPatient(patient);
            appointment.setDoctor(doctor);
            appointment.setStartTime(start);
            appointment.setEndTime(start.plusSeconds(1800));
            appointment.setStatus(AppointmentStatus.SCHEDULED);
            appointments.add(appointmentRepository.saveAndFlush(appointment));
        }

        entityManager.flush();
        entityManager.clear();

        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        PageResponse<DashboardAppointmentResponse> page =
                dashboardService.getTodayAppointments(PageRequest.of(0, 50));

        assertThat(page.content()).hasSize(5);
        assertThat(page.content())
                .extracting(DashboardAppointmentResponse::departmentName)
                .containsExactlyInAnyOrderElementsOf(departmentNames);
        assertThat(page.content())
                .extracting(DashboardAppointmentResponse::id)
                .containsExactlyElementsOf(appointments.stream().map(Appointment::getId).toList());
        assertThat(statistics.getPrepareStatementCount())
                .as("dashboard query should not lazily load patient/doctor/department per row")
                .isLessThanOrEqualTo(4L);
    }
}
