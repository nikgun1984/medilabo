package com.medilabo.assessment.service;

import com.medilabo.assessment.dto.AssessmentDTO;
import com.medilabo.assessment.dto.NoteDTO;
import com.medilabo.assessment.dto.PatientDTO;
import com.medilabo.assessment.model.RiskLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service that queries the Demographics and Notes microservices
 * and computes the diabetes risk assessment for a given patient.
 */
@Slf4j
@Service
public class AssessmentService {

    private final WebClient demographicsClient;
    private final WebClient notesClient;

    public AssessmentService(
            @Value("${demographics.service.url}") String demographicsUrl,
            @Value("${notes.service.url}") String notesUrl) {
        this.demographicsClient = WebClient.builder().baseUrl(demographicsUrl).build();
        this.notesClient = WebClient.builder().baseUrl(notesUrl).build();
        log.info("AssessmentService initialised — demographics={}, notes={}", demographicsUrl, notesUrl);
    }

    /**
     * Compute the diabetes risk assessment for the given patient id.
     */
    public AssessmentDTO assess(Long patId) {
        log.info("Starting assessment for patId={}", patId);

        // 1. Fetch patient demographics
        PatientDTO patient = fetchPatient(patId);
        log.info("Fetched patient: {} {} (dob={}, gender={})",
                patient.getFirstName(), patient.getLastName(),
                patient.getDateOfBirth(), patient.getGender());

        // 2. Fetch all notes for this patient
        List<NoteDTO> notes = fetchNotes(patId);
        log.info("Fetched {} note(s) for patId={}", notes.size(), patId);

        // 3. Concatenate all note texts
        String allNotesText = notes.stream()
                .map(NoteDTO::getNote)
                .collect(Collectors.joining(" "));

        // 4. Find trigger terms
        List<String> triggersFound = TriggerTerms.findTriggers(allNotesText);
        int triggerCount = triggersFound.size();
        log.info("Found {} trigger(s) for patId={}: {}", triggerCount, patId, triggersFound);

        // 5. Calculate age
        int age = Period.between(patient.getDateOfBirth(), LocalDate.now()).getYears();
        String gender = patient.getGender(); // "M" or "F"
        log.info("Patient age={}, gender={}", age, gender);

        // 6. Determine risk level
        RiskLevel riskLevel = determineRiskLevel(age, gender, triggerCount);
        log.info("Risk level for patId={}: {} (triggers={}, age={}, gender={})",
                patId, riskLevel, triggerCount, age, gender);

        return AssessmentDTO.builder()
                .patId(patId)
                .patientFullName(patient.getFirstName() + " " + patient.getLastName())
                .age(age)
                .gender(gender)
                .riskLevel(riskLevel.name())
                .triggerCount(triggerCount)
                .triggersFound(triggersFound)
                .build();
    }

    /**
     * Determine the risk level based on age, gender, and trigger count.
     *
     * Rules:
     * - None: default if no other level is matched
     * - Borderline: 2-5 triggers AND age > 30
     * - In Danger:
     *     - Male < 30: 3-4 triggers
     *     - Female < 30: 4-5 triggers
     *     - Age > 30: 6-7 triggers
     * - Early Onset:
     *     - Male < 30: >= 5 triggers
     *     - Female < 30: >= 6 triggers (was  7 in spec, but >= 7 triggers)
     *     - Age > 30: >= 8 triggers
     */
    RiskLevel determineRiskLevel(int age, String gender, int triggerCount) {
        boolean isUnder30 = age < 30;
        boolean isMale = "M".equalsIgnoreCase(gender);

        // Check Early Onset first (highest severity)
        if (isUnder30 && isMale && triggerCount >= 5) {
            return RiskLevel.EarlyOnset;
        }
        if (isUnder30 && !isMale && triggerCount >= 6) {
            return RiskLevel.EarlyOnset;
        }
        if (!isUnder30 && triggerCount >= 8) {
            return RiskLevel.EarlyOnset;
        }

        // Check In Danger
        if (isUnder30 && isMale && triggerCount >= 3) {
            return RiskLevel.InDanger;
        }
        if (isUnder30 && !isMale && triggerCount >= 4) {
            return RiskLevel.InDanger;
        }
        if (!isUnder30 && triggerCount >= 6) {
            return RiskLevel.InDanger;
        }

        // Check Borderline
        if (!isUnder30 && triggerCount >= 2) {
            return RiskLevel.Borderline;
        }

        return RiskLevel.None;
    }

    private PatientDTO fetchPatient(Long patId) {
        log.debug("Calling demographics service: GET /api/demographics/patients/{}", patId);
        return demographicsClient.get()
                .uri("/api/demographics/patients/{id}", patId)
                .retrieve()
                .bodyToMono(PatientDTO.class)
                .block();
    }

    private List<NoteDTO> fetchNotes(Long patId) {
        log.debug("Calling notes service: GET /api/notes/patient/{}", patId);
        return notesClient.get()
                .uri("/api/notes/patient/{patId}", patId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<NoteDTO>>() {})
                .block();
    }
}
