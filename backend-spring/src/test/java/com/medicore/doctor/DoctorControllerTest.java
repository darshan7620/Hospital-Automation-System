package com.medicore.doctor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicore.common.dto.PageResponse;
import com.medicore.common.exception.ResourceNotFoundException;
import com.medicore.doctor.dto.DoctorRequest;
import com.medicore.doctor.dto.DoctorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DoctorController.class)
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DoctorService doctorService;

    private static DoctorRequest validRequest() {
        return new DoctorRequest("Alice Smith", "Cardiologist", "+1-555-0100",
                "alice.smith@example.com", new BigDecimal("150.00"), 10L);
    }

    @Test
    void create_returns201_withCreatedDoctor() throws Exception {
        DoctorResponse response = new DoctorResponse(1L, "Alice Smith", "Cardiologist", "+1-555-0100",
                "alice.smith@example.com", new BigDecimal("150.00"), 10L, "Cardiology", true, null, null);
        when(doctorService.create(any(DoctorRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Alice Smith"))
                .andExpect(jsonPath("$.departmentName").value("Cardiology"));
    }

    @Test
    void create_returns400_withFieldErrors_whenInputInvalid() throws Exception {
        DoctorRequest invalid = new DoctorRequest("", "Cardiologist", null, "not-an-email",
                new BigDecimal("-1.00"), null);

        mockMvc.perform(post("/api/v1/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void getById_returns404_whenMissing() throws Exception {
        when(doctorService.getById(9L)).thenThrow(new ResourceNotFoundException("Doctor with id 9 was not found."));

        mockMvc.perform(get("/api/v1/doctors/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void list_returns200_withPagedContent() throws Exception {
        when(doctorService.list(any(), any(), any(), any(), any()))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, true));

        mockMvc.perform(get("/api/v1/doctors")
                        .param("name", "ali")
                        .param("specialization", "cardio")
                        .param("departmentId", "10")
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void deactivate_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/doctors/1"))
                .andExpect(status().isNoContent());
    }
}
