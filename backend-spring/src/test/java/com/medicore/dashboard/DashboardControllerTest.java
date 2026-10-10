package com.medicore.dashboard;

import com.medicore.appointment.AppointmentStatus;
import com.medicore.common.dto.PageResponse;
import com.medicore.dashboard.dto.AppointmentStatusCountResponse;
import com.medicore.dashboard.dto.DashboardAppointmentResponse;
import com.medicore.dashboard.dto.DashboardSummaryResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    private static final Instant START = Instant.parse("2030-01-01T09:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    private static DashboardSummaryResponse summary() {
        return new DashboardSummaryResponse(
                LocalDate.of(2030, 1, 1),
                START,
                3L,
                2L,
                1L,
                4L,
                5L,
                List.of(
                        new AppointmentStatusCountResponse(AppointmentStatus.SCHEDULED, 4L),
                        new AppointmentStatusCountResponse(AppointmentStatus.CONFIRMED, 1L)));
    }

    private static DashboardAppointmentResponse appointment() {
        return new DashboardAppointmentResponse(
                7L, 1L, "PAT-000001", "Test Patient",
                2L, "Dr. Test", 3L, "Cardiology",
                START, START.plusSeconds(1800), AppointmentStatus.SCHEDULED);
    }

    @Test
    void summary_returns200WithAggregates() throws Exception {
        when(dashboardService.getSummary()).thenReturn(summary());

        mockMvc.perform(get("/api/v1/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2030-01-01"))
                .andExpect(jsonPath("$.totalActivePatients").value(3))
                .andExpect(jsonPath("$.totalActiveDoctors").value(2))
                .andExpect(jsonPath("$.totalActiveDepartments").value(1))
                .andExpect(jsonPath("$.todayAppointments").value(4))
                .andExpect(jsonPath("$.upcomingAppointments").value(5))
                .andExpect(jsonPath("$.appointmentsByStatus[0].status").value("SCHEDULED"))
                .andExpect(jsonPath("$.appointmentsByStatus[0].count").value(4));
    }

    @Test
    void todayAppointments_returnsPagedContent() throws Exception {
        when(dashboardService.getTodayAppointments(any()))
                .thenReturn(new PageResponse<>(List.of(appointment()), 0, 20, 1, 1, true));

        mockMvc.perform(get("/api/v1/dashboard/appointments/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(7))
                .andExpect(jsonPath("$.content[0].patientNumber").value("PAT-000001"))
                .andExpect(jsonPath("$.content[0].doctorName").value("Dr. Test"))
                .andExpect(jsonPath("$.content[0].departmentName").value("Cardiology"))
                .andExpect(jsonPath("$.content[0].status").value("SCHEDULED"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void todayAppointments_boundsPageSizeAndNormalizesPage() throws Exception {
        when(dashboardService.getTodayAppointments(any()))
                .thenReturn(new PageResponse<>(List.of(), 0, 100, 0, 0, true));

        mockMvc.perform(get("/api/v1/dashboard/appointments/today")
                        .param("page", "-5")
                        .param("size", "1000"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(dashboardService).getTodayAppointments(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getPageSize()).isEqualTo(100);
    }

    @Test
    void upcomingAppointments_usesDefaultLimit10() throws Exception {
        when(dashboardService.getUpcomingAppointments(10)).thenReturn(List.of(appointment()));

        mockMvc.perform(get("/api/v1/dashboard/appointments/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"));

        verify(dashboardService).getUpcomingAppointments(10);
    }

    @Test
    void upcomingAppointments_clampsLimitToMaximum() throws Exception {
        when(dashboardService.getUpcomingAppointments(100)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/dashboard/appointments/upcoming").param("limit", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(dashboardService).getUpcomingAppointments(100);
    }

    @Test
    void upcomingAppointments_returns400WhenLimitNotNumeric() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/appointments/upcoming").param("limit", "abc"))
                .andExpect(status().isBadRequest());
    }
}
