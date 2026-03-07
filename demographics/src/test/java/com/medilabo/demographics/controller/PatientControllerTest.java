package com.medilabo.demographics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medilabo.demographics.dto.PatientDTO;
import com.medilabo.demographics.dto.PatientRequest;
import com.medilabo.demographics.exception.GlobalExceptionHandler;
import com.medilabo.demographics.exception.PatientNotFoundException;
import com.medilabo.demographics.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {PatientController.class, GlobalExceptionHandler.class})
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientService patientService;

    private ObjectMapper objectMapper;
    private PatientDTO patientDTO;
    private PatientRequest validRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        patientDTO = PatientDTO.builder()
                .id(1L)
                .lastName("TestNone")
                .firstName("Test")
                .dateOfBirth(LocalDate.of(1966, 12, 31))
                .gender("F")
                .address("1 Brookside St")
                .phone("100-222-3333")
                .createdAt(OffsetDateTime.now())
                .build();

        validRequest = new PatientRequest();
        validRequest.setLastName("TestNone");
        validRequest.setFirstName("Test");
        validRequest.setDateOfBirth(LocalDate.of(1966, 12, 31));
        validRequest.setGender("F");
        validRequest.setAddress("1 Brookside St");
        validRequest.setPhone("100-222-3333");
    }

    // ── GET /api/demographics/patients ──────────────────────────────────────

    @Test
    void getAllPatients_returns200WithList() throws Exception {
        when(patientService.getAllPatients()).thenReturn(List.of(patientDTO));

        mockMvc.perform(get("/api/demographics/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].lastName", is("TestNone")))
                .andExpect(jsonPath("$[0].gender", is("F")));
    }

    @Test
    void getAllPatients_returnsEmptyList() throws Exception {
        when(patientService.getAllPatients()).thenReturn(List.of());

        mockMvc.perform(get("/api/demographics/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ── GET /api/demographics/patients/{id} ─────────────────────────────────

    @Test
    void getPatientById_returns200WhenFound() throws Exception {
        when(patientService.getPatientById(1L)).thenReturn(patientDTO);

        mockMvc.perform(get("/api/demographics/patients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstName", is("Test")))
                .andExpect(jsonPath("$.dateOfBirth", is("1966-12-31")));
    }

    @Test
    void getPatientById_returns404WhenNotFound() throws Exception {
        when(patientService.getPatientById(99L)).thenThrow(new PatientNotFoundException(99L));

        mockMvc.perform(get("/api/demographics/patients/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("99")));
    }

    // ── POST /api/demographics/patients ─────────────────────────────────────

    @Test
    void createPatient_returns201WithBody() throws Exception {
        when(patientService.createPatient(any())).thenReturn(patientDTO);

        mockMvc.perform(post("/api/demographics/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.lastName", is("TestNone")));
    }

    @Test
    void createPatient_returns400WhenLastNameMissing() throws Exception {
        validRequest.setLastName("");

        mockMvc.perform(post("/api/demographics/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.lastName", notNullValue()));
    }

    @Test
    void createPatient_returns400WhenGenderInvalid() throws Exception {
        validRequest.setGender("X");

        mockMvc.perform(post("/api/demographics/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.gender", notNullValue()));
    }

    @Test
    void createPatient_returns400WhenDateOfBirthMissing() throws Exception {
        validRequest.setDateOfBirth(null);

        mockMvc.perform(post("/api/demographics/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.dateOfBirth", notNullValue()));
    }

    @Test
    void createPatient_succeeds_withNoAddressOrPhone() throws Exception {
        validRequest.setAddress(null);
        validRequest.setPhone(null);
        when(patientService.createPatient(any())).thenReturn(
                PatientDTO.builder().id(2L).lastName("TestNone").firstName("Test")
                        .dateOfBirth(LocalDate.of(1966, 12, 31)).gender("F")
                        .createdAt(OffsetDateTime.now()).build());

        mockMvc.perform(post("/api/demographics/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(2)));
    }

    // ── PUT /api/demographics/patients/{id} ─────────────────────────────────

    @Test
    void updatePatient_returns200WithUpdatedBody() throws Exception {
        validRequest.setLastName("Updated");
        PatientDTO updated = PatientDTO.builder()
                .id(1L).lastName("Updated").firstName("Test")
                .dateOfBirth(LocalDate.of(1966, 12, 31)).gender("F")
                .createdAt(OffsetDateTime.now()).build();
        when(patientService.updatePatient(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/demographics/patients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName", is("Updated")));
    }

    @Test
    void updatePatient_returns404WhenNotFound() throws Exception {
        when(patientService.updatePatient(eq(99L), any()))
                .thenThrow(new PatientNotFoundException(99L));

        mockMvc.perform(put("/api/demographics/patients/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("99")));
    }

    @Test
    void updatePatient_returns400WhenBodyInvalid() throws Exception {
        validRequest.setFirstName("");

        mockMvc.perform(put("/api/demographics/patients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstName", notNullValue()));
    }
}
