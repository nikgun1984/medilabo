package com.medilabo.demographics.controller;

import com.medilabo.demographics.dto.PatientDTO;
import com.medilabo.demographics.dto.PatientRequest;
import com.medilabo.demographics.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/demographics/patients")
@RequiredArgsConstructor
/**
 * Controller for managing patients. Provides endpoints to list, retrieve, create, and update patient records.
 */
public class PatientController {

    private final PatientService patientService;

    /** GET /api/demographics/patients — list all patients */
    @GetMapping
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        log.info("GET /api/demographics/patients");
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    /** GET /api/demographics/patients/{id} — get one patient */
    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable Long id) {
        log.info("GET /api/demographics/patients/{}", id);
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    /** POST /api/demographics/patients — create a new patient */
    @PostMapping
    public ResponseEntity<PatientDTO> createPatient(@Valid @RequestBody PatientRequest request) {
        log.info("POST /api/demographics/patients — lastName={}", request.getLastName());
        PatientDTO created = patientService.createPatient(request);
        log.info("Patient created with id={}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** PUT /api/demographics/patients/{id} — update an existing patient */
    @PutMapping("/{id}")
    public ResponseEntity<PatientDTO> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequest request) {
        log.info("PUT /api/demographics/patients/{}", id);
        return ResponseEntity.ok(patientService.updatePatient(id, request));
    }
}
