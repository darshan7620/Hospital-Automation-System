package com.medicore.patient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicore.common.dto.PageResponse;
import com.medicore.common.exception.ResourceNotFoundException;
import com.medicore.patient.dto.PatientRequest;
import com.medicore.patient.dto.PatientResponse;
import com.medicore.patient.dto.PatientSummaryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PatientService patientService;

    private static PatientRequest validRequest() {
        return new PatientRequest("John Doe", LocalDate.of(1990, 1, 1), "+1-555-0100", "123 Main St");
    }

    @Test
    void create_returns201_withGeneratedPatientNumber() throws Exception {
        PatientResponse response = new PatientResponse(1L, "PAT-000001", "John Doe",
                LocalDate.of(1990, 1, 1), "+1-555-0100", "123 Main St", true, Instant.now(), Instant.now());
        when(patientService.create(any(PatientRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientNumber").value("PAT-000001"))
                .andExpect(jsonPath("$.fullName").value("John Doe"));
    }

    @Test
    void create_returns400_whenRequiredFieldsMissing() throws Exception {
        PatientRequest invalid = new PatientRequest("", null, "abc", null);

        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void create_returns400_whenDateOfBirthIsInTheFuture() throws Exception {
        PatientRequest future = new PatientRequest("John Doe", LocalDate.of(2999, 1, 1), "+1-555-0100", null);

        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(future)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("dateOfBirth"));
    }

    @Test
    void create_returns400_whenBodyIsMalformed() throws Exception {
        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"John\",\"dateOfBirth\":\"not-a-date\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getById_returns404_whenMissing() throws Exception {
        when(patientService.getById(5L)).thenThrow(new ResourceNotFoundException("Patient with id 5 was not found."));

        mockMvc.perform(get("/api/v1/patients/5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void list_returns200_withPagedSummaries() throws Exception {
        when(patientService.list(any(), any(), any(), any()))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, true));

        mockMvc.perform(get("/api/v1/patients")
                        .param("name", "john")
                        .param("phone", "555")
                        .param("active", "true")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void deactivate_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/patients/1"))
                .andExpect(status().isNoContent());
    }
}
