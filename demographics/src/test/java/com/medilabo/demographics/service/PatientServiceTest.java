package com.medilabo.demographics.service;

import com.medilabo.demographics.dto.PatientDTO;
import com.medilabo.demographics.dto.PatientRequest;
import com.medilabo.demographics.exception.PatientNotFoundException;
import com.medilabo.demographics.model.Patient;
import com.medilabo.demographics.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient patient;
    private PatientRequest request;

    @BeforeEach
    void setUp() {
        patient = Patient.builder()
                .id(1L)
                .lastName("TestNone")
                .firstName("Test")
                .dateOfBirth(LocalDate.of(1966, 12, 31))
                .gender("F")
                .address("1 Brookside St")
                .phone("100-222-3333")
                .createdAt(OffsetDateTime.now())
                .build();

        request = new PatientRequest();
        request.setLastName("TestNone");
        request.setFirstName("Test");
        request.setDateOfBirth(LocalDate.of(1966, 12, 31));
        request.setGender("F");
        request.setAddress("1 Brookside St");
        request.setPhone("100-222-3333");
    }

    // ── getAllPatients ───────────────────────────────────────────────────────

    @Test
    void getAllPatients_returnsAllPatients() {
        when(patientRepository.findAll()).thenReturn(List.of(patient));

        List<PatientDTO> result = patientService.getAllPatients();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLastName()).isEqualTo("TestNone");
        verify(patientRepository).findAll();
    }

    @Test
    void getAllPatients_returnsEmptyListWhenNoneExist() {
        when(patientRepository.findAll()).thenReturn(List.of());

        assertThat(patientService.getAllPatients()).isEmpty();
    }

    // ── getPatientById ───────────────────────────────────────────────────────

    @Test
    void getPatientById_returnsPatientWhenFound() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        PatientDTO result = patientService.getPatientById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("Test");
        assertThat(result.getGender()).isEqualTo("F");
    }

    @Test
    void getPatientById_throwsWhenNotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getPatientById(99L))
                .isInstanceOf(PatientNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── createPatient ────────────────────────────────────────────────────────

    @Test
    void createPatient_savesAndReturnsDTO() {
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        PatientDTO result = patientService.createPatient(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLastName()).isEqualTo("TestNone");
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void createPatient_withOptionalFieldsNull_savesSuccessfully() {
        request.setAddress(null);
        request.setPhone(null);
        Patient noOptional = Patient.builder()
                .id(2L).lastName("Min").firstName("Patient")
                .dateOfBirth(LocalDate.of(2000, 1, 1)).gender("M")
                .createdAt(OffsetDateTime.now()).build();
        when(patientRepository.save(any(Patient.class))).thenReturn(noOptional);

        PatientDTO result = patientService.createPatient(request);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getAddress()).isNull();
        assertThat(result.getPhone()).isNull();
    }

    // ── updatePatient ────────────────────────────────────────────────────────

    @Test
    void updatePatient_updatesFieldsAndReturnsDTO() {
        request.setLastName("Updated");
        request.setAddress("99 New Road");
        Patient updated = Patient.builder()
                .id(1L).lastName("Updated").firstName("Test")
                .dateOfBirth(LocalDate.of(1966, 12, 31)).gender("F")
                .address("99 New Road").phone("100-222-3333")
                .createdAt(patient.getCreatedAt()).build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenReturn(updated);

        PatientDTO result = patientService.updatePatient(1L, request);

        assertThat(result.getLastName()).isEqualTo("Updated");
        assertThat(result.getAddress()).isEqualTo("99 New Road");
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void updatePatient_throwsWhenPatientNotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.updatePatient(99L, request))
                .isInstanceOf(PatientNotFoundException.class)
                .hasMessageContaining("99");

        verify(patientRepository, never()).save(any());
    }
}
