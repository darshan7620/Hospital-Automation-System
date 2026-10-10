package com.medicore.appointment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicore.appointment.dto.AppointmentRequest;
import com.medicore.appointment.dto.AppointmentResponse;
import com.medicore.common.dto.PageResponse;
import com.medicore.common.exception.BadRequestException;
import com.medicore.common.exception.ConflictException;
import com.medicore.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
class AppointmentControllerTest {

    private static final Instant START = Instant.parse("2030-01-01T09:00:00Z");
    private static final Instant END = Instant.parse("2030-01-01T09:30:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    private static AppointmentResponse response() {
        return new AppointmentResponse(1L, 1L, "PAT-000001", "Test Patient", 2L, "Dr. Test",
                3L, "Cardiology", START, END, "Checkup", AppointmentStatus.SCHEDULED, null, null);
    }

    @Test
    void create_returns201() throws Exception {
        when(appointmentService.create(any(AppointmentRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AppointmentRequest(1L, 2L, START, END, "Checkup"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.doctorName").value("Dr. Test"));
    }

    @Test
    void create_returns400_whenRequiredFieldsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void create_returns400_whenTimeRangeInvalid() throws Exception {
        when(appointmentService.create(any(AppointmentRequest.class)))
                .thenThrow(new BadRequestException("endTime must be after startTime."));

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AppointmentRequest(1L, 2L, END, START, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void create_returns409_whenBookingConflicts() throws Exception {
        when(appointmentService.create(any(AppointmentRequest.class)))
                .thenThrow(new ConflictException("overlapping"));

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AppointmentRequest(1L, 2L, START, END, null))))
                .andExpect(status().isConflict());
    }

    @Test
    void create_returns404_whenReferenceMissing() throws Exception {
        when(appointmentService.create(any(AppointmentRequest.class)))
                .thenThrow(new ResourceNotFoundException("Patient with id 1 was not found."));

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AppointmentRequest(1L, 2L, START, END, null))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_returns404_whenMissing() throws Exception {
        when(appointmentService.getById(9L)).thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(get("/api/v1/appointments/9"))
                .andExpect(status().isNotFound());
    }

    @Test
    void list_returns200_withFilters() throws Exception {
        when(appointmentService.list(any(), any(), any(), any(), any()))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, true));

        mockMvc.perform(get("/api/v1/appointments")
                        .param("date", "2030-01-01")
                        .param("doctorId", "2")
                        .param("patientId", "1")
                        .param("status", "SCHEDULED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void list_returns400_whenStatusInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/appointments").param("status", "NOT_A_STATUS"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancel_returns200_withCancelledStatus() throws Exception {
        AppointmentResponse cancelled = new AppointmentResponse(1L, 1L, "PAT-000001", "Test Patient",
                2L, "Dr. Test", 3L, "Cardiology", START, END, "Checkup", AppointmentStatus.CANCELLED, null, null);
        when(appointmentService.cancel(eq(1L))).thenReturn(cancelled);

        mockMvc.perform(post("/api/v1/appointments/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
