package com.medicore.dashboard;

import com.medicore.appointment.Appointment;
import com.medicore.appointment.AppointmentRepository;
import com.medicore.appointment.AppointmentStatus;
import com.medicore.common.dto.PageResponse;
import com.medicore.dashboard.dto.AppointmentStatusCountResponse;
import com.medicore.dashboard.dto.DashboardAppointmentResponse;
import com.medicore.dashboard.dto.DashboardSummaryResponse;
import com.medicore.department.Department;
import com.medicore.department.DepartmentRepository;
import com.medicore.doctor.Doctor;
import com.medicore.doctor.DoctorRepository;
import com.medicore.patient.Patient;
import com.medicore.patient.PatientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class DashboardIntegrationTest {

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

    private Department newDepartment(boolean active) {
        Department department = new Department();
        department.setName("Dept-" + UUID.randomUUID().toString().substring(0, 8));
        department.setActive(active);
        return departmentRepository.saveAndFlush(department);
    }

    private Doctor newDoctor(Department department, boolean active) {
        Doctor doctor = new Doctor();
        doctor.setFullName("Dr-" + UUID.randomUUID().toString().substring(0, 8));
        doctor.setSpecialization("General");
        doctor.setEmail("doc-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        doctor.setConsultationFee(new BigDecimal("100.00"));
        doctor.setDepartment(department);
        doctor.setActive(active);
        return doctorRepository.saveAndFlush(doctor);
    }

    private Patient newPatient(boolean active) {
        Patient patient = new Patient();
        patient.setPatientNumber("T-" + UUID.randomUUID().toString().substring(0, 16));
        patient.setFullName("Patient-" + UUID.randomUUID().toString().substring(0, 8));
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient.setPhone("+1-555-0100");
        patient.setActive(active);
        return patientRepository.saveAndFlush(patient);
    }

    private Appointment newAppointment(Patient patient, Doctor doctor, Instant start, Instant end,
                                       AppointmentStatus status) {
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setStartTime(start);
        appointment.setEndTime(end);
        appointment.setStatus(status);
        return appointmentRepository.saveAndFlush(appointment);
    }

    private static Map<AppointmentStatus, Long> statusMap(DashboardSummaryResponse summary) {
        return summary.appointmentsByStatus().stream()
                .collect(Collectors.toMap(AppointmentStatusCountResponse::status,
                        AppointmentStatusCountResponse::count));
    }

    @Test
    void summary_countsOnlyActiveEntities() {
        DashboardSummaryResponse before = dashboardService.getSummary();

        Department activeDepartment = newDepartment(true);
        newDepartment(false);
        newDoctor(activeDepartment, true);
        newDoctor(activeDepartment, false);
        newPatient(true);
        newPatient(true);
        newPatient(false);

        DashboardSummaryResponse after = dashboardService.getSummary();

        assertThat(after.totalActiveDepartments() - before.totalActiveDepartments()).isEqualTo(1L);
        assertThat(after.totalActiveDoctors() - before.totalActiveDoctors()).isEqualTo(1L);
        assertThat(after.totalActivePatients() - before.totalActivePatients()).isEqualTo(2L);
    }

    @Test
    void summary_countsEveryStatusAndOnlyActiveUpcomingAppointments() {
        DashboardSummaryResponse before = dashboardService.getSummary();

        Department department = newDepartment(true);
        Doctor doctor = newDoctor(department, true);
        Patient patient = newPatient(true);
        Instant base = Instant.now().plusSeconds(30L * 24 * 3600);

        int offset = 0;
        for (AppointmentStatus status : AppointmentStatus.values()) {
            Instant start = base.plusSeconds(offset);
            newAppointment(patient, doctor, start, start.plusSeconds(1800), status);
            offset += 3600;
        }

        DashboardSummaryResponse after = dashboardService.getSummary();
        Map<AppointmentStatus, Long> beforeStatus = statusMap(before);
        Map<AppointmentStatus, Long> afterStatus = statusMap(after);

        for (AppointmentStatus status : AppointmentStatus.values()) {
            assertThat(afterStatus.get(status) - beforeStatus.get(status))
                    .as("count delta for %s", status)
                    .isEqualTo(1L);
        }
        assertThat(after.upcomingAppointments() - before.upcomingAppointments()).isEqualTo(2L);
        assertThat(after.todayAppointments() - before.todayAppointments()).isZero();
    }

    @Test
    void todayAppointments_respectUtcDayBoundaryAndOrdering() {
        Department department = newDepartment(true);
        Doctor doctor = newDoctor(department, true);
        Patient patient = newPatient(true);

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Instant dayStart = today.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endOfDay = dayStart.plusSeconds(24 * 3600);

        Appointment lateToday = newAppointment(patient, doctor,
                endOfDay.minusSeconds(1), endOfDay.minusSeconds(1).plusSeconds(60), AppointmentStatus.SCHEDULED);
        Appointment earlyToday = newAppointment(patient, doctor,
                dayStart.plusSeconds(3600), dayStart.plusSeconds(5400), AppointmentStatus.SCHEDULED);
        Appointment nextMidnight = newAppointment(patient, doctor,
                endOfDay, endOfDay.plusSeconds(1800), AppointmentStatus.SCHEDULED);

        PageResponse<DashboardAppointmentResponse> page =
                dashboardService.getTodayAppointments(PageRequest.of(0, 100));

        List<Long> ids = page.content().stream().map(DashboardAppointmentResponse::id).toList();
        assertThat(ids).containsExactly(earlyToday.getId(), lateToday.getId());
        assertThat(ids).doesNotContain(nextMidnight.getId());
    }

    @Test
    void upcomingAppointments_orderByStartAndExcludeTerminalStatuses() {
        Department department = newDepartment(true);
        Doctor doctor = newDoctor(department, true);
        Patient patient = newPatient(true);
        Instant now = Instant.now();

        Appointment cancelled = newAppointment(patient, doctor,
                now.plusSeconds(86400), now.plusSeconds(86400 + 1800), AppointmentStatus.CANCELLED);
        Appointment completed = newAppointment(patient, doctor,
                now.plusSeconds(2 * 86400), now.plusSeconds(2 * 86400 + 1800), AppointmentStatus.COMPLETED);
        Appointment confirmed = newAppointment(patient, doctor,
                now.plusSeconds(3 * 86400), now.plusSeconds(3 * 86400 + 1800), AppointmentStatus.CONFIRMED);
        Appointment noShow = newAppointment(patient, doctor,
                now.plusSeconds(4 * 86400), now.plusSeconds(4 * 86400 + 1800), AppointmentStatus.NO_SHOW);
        Appointment scheduled = newAppointment(patient, doctor,
                now.plusSeconds(5 * 86400), now.plusSeconds(5 * 86400 + 1800), AppointmentStatus.SCHEDULED);

        List<DashboardAppointmentResponse> upcoming = dashboardService.getUpcomingAppointments(100);

        assertThat(upcoming).allMatch(item -> item.status() == AppointmentStatus.SCHEDULED
                || item.status() == AppointmentStatus.CONFIRMED);
        List<Long> ids = upcoming.stream().map(DashboardAppointmentResponse::id).toList();
        assertThat(ids).containsSubsequence(confirmed.getId(), scheduled.getId());
        assertThat(ids).doesNotContain(cancelled.getId(), completed.getId(), noShow.getId());
    }

    @Test
    void todayAppointments_supportPagination() {
        Department department = newDepartment(true);
        Doctor doctor = newDoctor(department, true);
        Patient patient = newPatient(true);
        Instant dayStart = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant();

        Appointment first = newAppointment(patient, doctor,
                dayStart.plusSeconds(8 * 3600), dayStart.plusSeconds(8 * 3600 + 1800), AppointmentStatus.SCHEDULED);
        Appointment second = newAppointment(patient, doctor,
                dayStart.plusSeconds(9 * 3600), dayStart.plusSeconds(9 * 3600 + 1800), AppointmentStatus.SCHEDULED);
        Appointment third = newAppointment(patient, doctor,
                dayStart.plusSeconds(10 * 3600), dayStart.plusSeconds(10 * 3600 + 1800), AppointmentStatus.SCHEDULED);

        PageResponse<DashboardAppointmentResponse> page0 =
                dashboardService.getTodayAppointments(PageRequest.of(0, 1));
        PageResponse<DashboardAppointmentResponse> page1 =
                dashboardService.getTodayAppointments(PageRequest.of(1, 1));
        PageResponse<DashboardAppointmentResponse> page2 =
                dashboardService.getTodayAppointments(PageRequest.of(2, 1));

        assertThat(page0.totalElements()).isEqualTo(3L);
        assertThat(page0.totalPages()).isEqualTo(3);
        assertThat(page0.content()).extracting(DashboardAppointmentResponse::id)
                .containsExactly(first.getId());
        assertThat(page1.content()).extracting(DashboardAppointmentResponse::id)
                .containsExactly(second.getId());
        assertThat(page2.content()).extracting(DashboardAppointmentResponse::id)
                .containsExactly(third.getId());
    }

    @Test
    void todayAppointments_includePatientDoctorAndDepartmentInformation() {
        Department department = newDepartment(true);
        department.setName("Cardiology");
        departmentRepository.saveAndFlush(department);
        Doctor doctor = newDoctor(department, true);
        doctor.setFullName("Dr. House");
        doctorRepository.saveAndFlush(doctor);
        Patient patient = newPatient(true);
        patient.setFullName("Gregory House");
        patientRepository.saveAndFlush(patient);
        Instant dayStart = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant start = dayStart.plusSeconds(11 * 3600);
        Appointment appointment = newAppointment(patient, doctor, start, start.plusSeconds(1800),
                AppointmentStatus.CONFIRMED);

        PageResponse<DashboardAppointmentResponse> page =
                dashboardService.getTodayAppointments(PageRequest.of(0, 100));

        DashboardAppointmentResponse row = page.content().stream()
                .filter(item -> item.id().equals(appointment.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(row.patientNumber()).isEqualTo(patient.getPatientNumber());
        assertThat(row.patientName()).isEqualTo("Gregory House");
        assertThat(row.doctorName()).isEqualTo("Dr. House");
        assertThat(row.departmentName()).isEqualTo("Cardiology");
        assertThat(row.startTime()).isEqualTo(start);
        assertThat(row.endTime()).isEqualTo(start.plusSeconds(1800));
        assertThat(row.status()).isEqualTo(AppointmentStatus.CONFIRMED);
    }
}
