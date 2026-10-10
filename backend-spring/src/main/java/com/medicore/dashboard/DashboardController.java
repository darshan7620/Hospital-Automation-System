package com.medicore.dashboard;

import com.medicore.common.dto.PageResponse;
import com.medicore.dashboard.dto.DashboardAppointmentResponse;
import com.medicore.dashboard.dto.DashboardSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard")
public class DashboardController {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_UPCOMING_LIMIT = 100;

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Aggregated dashboard summary")
    public DashboardSummaryResponse summary() {
        return dashboardService.getSummary();
    }

    @GetMapping("/appointments/today")
    @Operation(summary = "Today's appointments (UTC day), ordered by start time")
    public PageResponse<DashboardAppointmentResponse> todayAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int boundedSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return dashboardService.getTodayAppointments(PageRequest.of(Math.max(page, 0), boundedSize));
    }

    @GetMapping("/appointments/upcoming")
    @Operation(summary = "Upcoming active appointments, ordered by start time")
    public List<DashboardAppointmentResponse> upcomingAppointments(
            @RequestParam(defaultValue = "10") int limit) {
        int boundedLimit = Math.min(Math.max(limit, 1), MAX_UPCOMING_LIMIT);
        return dashboardService.getUpcomingAppointments(boundedLimit);
    }
}
