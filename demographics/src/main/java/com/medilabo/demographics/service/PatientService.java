package com.medilabo.demographics.service;

import com.medilabo.demographics.dto.PatientDTO;
import com.medilabo.demographics.dto.PatientRequest;
import com.medilabo.demographics.exception.PatientNotFoundException;
import com.medilabo.demographics.model.Patient;
import com.medilabo.demographics.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    @Transactional(readOnly = true)
    public List<PatientDTO> getAllPatients() {
        log.info("Fetching all patients");
        List<PatientDTO> patients = patientRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
        log.debug("Found {} patient(s)", patients.size());
        return patients;
    }

    @Transactional(readOnly = true)
    public PatientDTO getPatientById(Long id) {
        log.info("Fetching patient with id={}", id);
        return patientRepository.findById(id)
                .map(p -> { log.debug("Found patient id={}", id); return toDTO(p); })
                .orElseThrow(() -> {
                    log.warn("Patient not found with id={}", id);
                    return new PatientNotFoundException(id);
                });
    }

    @Transactional
    public PatientDTO createPatient(PatientRequest request) {
        log.info("Creating patient: lastName={}, firstName={}", request.getLastName(), request.getFirstName());
        Patient patient = toEntity(request);
        PatientDTO saved = toDTO(patientRepository.save(patient));
        log.info("Created patient with id={}", saved.getId());
        return saved;
    }

    @Transactional
    public PatientDTO updatePatient(Long id, PatientRequest request) {
        log.info("Updating patient with id={}", id);
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cannot update — patient not found with id={}", id);
                    return new PatientNotFoundException(id);
                });

        patient.setLastName(request.getLastName());
        patient.setFirstName(request.getFirstName());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setAddress(request.getAddress());
        patient.setPhone(request.getPhone());

        PatientDTO updated = toDTO(patientRepository.save(patient));
        log.info("Updated patient with id={}", id);
        return updated;
    }

    // ── mapping helpers ─────────────────────────────────────────────────────

    private PatientDTO toDTO(Patient patient) {
        return PatientDTO.builder()
                .id(patient.getId())
                .lastName(patient.getLastName())
                .firstName(patient.getFirstName())
                .dateOfBirth(patient.getDateOfBirth())
                .gender(patient.getGender())
                .address(patient.getAddress())
                .phone(patient.getPhone())
                .createdAt(patient.getCreatedAt())
                .build();
    }

    private Patient toEntity(PatientRequest request) {
        return Patient.builder()
                .lastName(request.getLastName())
                .firstName(request.getFirstName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .address(request.getAddress())
                .phone(request.getPhone())
                .build();
    }
}
