package com.medicore.dashboard;

import com.medicore.appointment.Appointment;
import com.medicore.appointment.AppointmentRepository;
import com.medicore.appointment.AppointmentStatus;
import com.medicore.appointment.AppointmentStatusCount;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private static AppointmentStatusCount statusCount(AppointmentStatus status, long total) {
        return new AppointmentStatusCount() {
            @Override
            public AppointmentStatus getStatus() {
                return status;
            }

            @Override
            public long getTotal() {
                return total;
            }
        };
    }

    private static Appointment appointment(long id, AppointmentStatus status, Instant start, Instant end) {
        Department department = new Department();
        department.setId(3L);
        department.setName("Cardiology");

        Doctor doctor = new Doctor();
        doctor.setId(2L);
        doctor.setFullName("Dr. Test");
        doctor.setDepartment(department);

        Patient patient = new Patient();
        patient.setId(1L);
        patient.setPatientNumber("PAT-000001");
        patient.setFullName("Test Patient");

        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setStatus(status);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setStartTime(start);
        appointment.setEndTime(end);
        return appointment;
    }

    @Test
    void getSummary_returnsZeroCountsAndEveryStatusForEmptyDatabase() {
        when(patientRepository.countByActiveTrue()).thenReturn(0L);
        when(doctorRepository.countByActiveTrue()).thenReturn(0L);
        when(departmentRepository.countByActiveTrue()).thenReturn(0L);
        when(appointmentRepository.countByStartTimeGreaterThanEqualAndStartTimeLessThan(any(), any()))
                .thenReturn(0L);
        when(appointmentRepository.countByStartTimeGreaterThanEqualAndStatusIn(any(), any()))
                .thenReturn(0L);
        when(appointmentRepository.countGroupedByStatus()).thenReturn(List.of());

        DashboardSummaryResponse summary = dashboardService.getSummary();

        assertThat(summary.date()).isEqualTo(LocalDate.now(ZoneOffset.UTC));
        assertThat(summary.generatedAt()).isNotNull();
        assertThat(summary.totalActivePatients()).isZero();
        assertThat(summary.totalActiveDoctors()).isZero();
        assertThat(summary.totalActiveDepartments()).isZero();
        assertThat(summary.todayAppointments()).isZero();
        assertThat(summary.upcomingAppointments()).isZero();
        assertThat(summary.appointmentsByStatus())
                .extracting(AppointmentStatusCountResponse::status)
                .containsExactly(AppointmentStatus.values());
        assertThat(summary.appointmentsByStatus())
                .allSatisfy(item -> assertThat(item.count()).isZero());
    }

    @Test
    void getSummary_aggregatesCountsAndGroupsByStatus() {
        when(patientRepository.countByActiveTrue()).thenReturn(3L);
        when(doctorRepository.countByActiveTrue()).thenReturn(2L);
        when(departmentRepository.countByActiveTrue()).thenReturn(1L);
        when(appointmentRepository.countByStartTimeGreaterThanEqualAndStartTimeLessThan(any(), any()))
                .thenReturn(4L);
        when(appointmentRepository.countByStartTimeGreaterThanEqualAndStatusIn(any(), any()))
                .thenReturn(5L);
        when(appointmentRepository.countGroupedByStatus()).thenReturn(List.of(
                statusCount(AppointmentStatus.SCHEDULED, 2L),
                statusCount(AppointmentStatus.CANCELLED, 1L)));

        DashboardSummaryResponse summary = dashboardService.getSummary();

        assertThat(summary.totalActivePatients()).isEqualTo(3L);
        assertThat(summary.totalActiveDoctors()).isEqualTo(2L);
        assertThat(summary.totalActiveDepartments()).isEqualTo(1L);
        assertThat(summary.todayAppointments()).isEqualTo(4L);
        assertThat(summary.upcomingAppointments()).isEqualTo(5L);

        Map<AppointmentStatus, Long> counts = summary.appointmentsByStatus().stream()
                .collect(Collectors.toMap(AppointmentStatusCountResponse::status,
                        AppointmentStatusCountResponse::count));
        assertThat(counts).containsEntry(AppointmentStatus.SCHEDULED, 2L)
                .containsEntry(AppointmentStatus.CANCELLED, 1L)
                .containsEntry(AppointmentStatus.CONFIRMED, 0L)
                .containsEntry(AppointmentStatus.COMPLETED, 0L)
                .containsEntry(AppointmentStatus.NO_SHOW, 0L);
    }

    @Test
    void getTodayAppointments_usesUtcDayBoundaryAndMapsRows() {
        Instant start = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant()
                .plusSeconds(3600);
        Page<Appointment> page = new PageImpl<>(
                List.of(appointment(7L, AppointmentStatus.SCHEDULED, start, start.plusSeconds(1800))),
                PageRequest.of(0, 20),
                1);
        when(appointmentRepository.findTodayWithDetails(any(), any(), any())).thenReturn(page);

        PageResponse<DashboardAppointmentResponse> result =
                dashboardService.getTodayAppointments(PageRequest.of(0, 20));

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).patientNumber()).isEqualTo("PAT-000001");
        assertThat(result.content().get(0).doctorName()).isEqualTo("Dr. Test");
        assertThat(result.content().get(0).departmentName()).isEqualTo("Cardiology");

        ArgumentCaptor<Instant> fromCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> toCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(appointmentRepository).findTodayWithDetails(fromCaptor.capture(), toCaptor.capture(), any());

        Instant expectedFrom = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant();
        assertThat(fromCaptor.getValue()).isEqualTo(expectedFrom);
        assertThat(toCaptor.getValue().minusSeconds(86400)).isEqualTo(expectedFrom);
    }

    @Test
    void getUpcomingAppointments_queriesActiveStatusesWithBoundedLimit() {
        Instant start = Instant.now().plusSeconds(3600);
        when(appointmentRepository.findUpcomingWithDetails(any(), any(), any()))
                .thenReturn(List.of(appointment(9L, AppointmentStatus.CONFIRMED, start, start.plusSeconds(1800))));

        List<DashboardAppointmentResponse> result = dashboardService.getUpcomingAppointments(5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(AppointmentStatus.CONFIRMED);

        ArgumentCaptor<Set<AppointmentStatus>> statusCaptor = ArgumentCaptor.forClass(Set.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(appointmentRepository).findUpcomingWithDetails(any(), statusCaptor.capture(), pageableCaptor.capture());

        assertThat(statusCaptor.getValue())
                .containsExactlyInAnyOrder(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(5);
        assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
    }

    @Test
    void getUpcomingAppointments_clampsNonPositiveLimitToOne() {
        when(appointmentRepository.findUpcomingWithDetails(any(), any(), any())).thenReturn(List.of());

        List<DashboardAppointmentResponse> result = dashboardService.getUpcomingAppointments(0);

        assertThat(result).isEmpty();
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(appointmentRepository).findUpcomingWithDetails(any(), any(), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(1);
    }

    @Test
    void getUpcomingAppointments_returnsEmptyListWhenNone() {
        when(appointmentRepository.findUpcomingWithDetails(any(), any(), any())).thenReturn(List.of());

        assertThat(dashboardService.getUpcomingAppointments(10)).isEmpty();
    }
}
