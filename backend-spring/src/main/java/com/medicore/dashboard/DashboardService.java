package com.medicore.dashboard;

import com.medicore.appointment.AppointmentRepository;
import com.medicore.appointment.AppointmentStatus;
import com.medicore.appointment.AppointmentStatusCount;
import com.medicore.common.dto.PageResponse;
import com.medicore.dashboard.dto.AppointmentStatusCountResponse;
import com.medicore.dashboard.dto.DashboardAppointmentResponse;
import com.medicore.dashboard.dto.DashboardSummaryResponse;
import com.medicore.department.DepartmentRepository;
import com.medicore.doctor.DoctorRepository;
import com.medicore.patient.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    static final Set<AppointmentStatus> UPCOMING_STATUSES =
            Set.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED);

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public DashboardService(DepartmentRepository departmentRepository,
                            DoctorRepository doctorRepository,
                            PatientRepository patientRepository,
                            AppointmentRepository appointmentRepository) {
        this.departmentRepository = departmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public DashboardSummaryResponse getSummary() {
        Instant now = Instant.now();
        LocalDate date = now.atZone(ZoneOffset.UTC).toLocalDate();

        long totalActivePatients = patientRepository.countByActiveTrue();
        long totalActiveDoctors = doctorRepository.countByActiveTrue();
        long totalActiveDepartments = departmentRepository.countByActiveTrue();
        long todayAppointments = appointmentRepository
                .countByStartTimeGreaterThanEqualAndStartTimeLessThan(from(date), until(date));
        long upcomingAppointments = appointmentRepository
                .countByStartTimeGreaterThanEqualAndStatusIn(now, UPCOMING_STATUSES);

        Map<AppointmentStatus, Long> grouped = new EnumMap<>(AppointmentStatus.class);
        for (AppointmentStatusCount row : appointmentRepository.countGroupedByStatus()) {
            grouped.put(row.getStatus(), row.getTotal());
        }
        List<AppointmentStatusCountResponse> byStatus = Arrays.stream(AppointmentStatus.values())
                .map(status -> new AppointmentStatusCountResponse(status, grouped.getOrDefault(status, 0L)))
                .toList();

        return new DashboardSummaryResponse(
                date,
                now,
                totalActivePatients,
                totalActiveDoctors,
                totalActiveDepartments,
                todayAppointments,
                upcomingAppointments,
                byStatus);
    }

    public PageResponse<DashboardAppointmentResponse> getTodayAppointments(Pageable pageable) {
        LocalDate date = LocalDate.now(ZoneOffset.UTC);
        Page<DashboardAppointmentResponse> page = appointmentRepository
                .findTodayWithDetails(from(date), until(date), pageable)
                .map(DashboardMapper::toAppointment);
        return PageResponse.from(page);
    }

    public List<DashboardAppointmentResponse> getUpcomingAppointments(int limit) {
        Pageable pageable = PageRequest.of(0, Math.max(limit, 1));
        return appointmentRepository
                .findUpcomingWithDetails(Instant.now(), UPCOMING_STATUSES, pageable)
                .stream()
                .map(DashboardMapper::toAppointment)
                .toList();
    }

    private static Instant from(LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    private static Instant until(LocalDate date) {
        return date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}
