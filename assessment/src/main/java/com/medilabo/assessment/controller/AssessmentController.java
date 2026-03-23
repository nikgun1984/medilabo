package com.medilabo.assessment.controller;

import com.medilabo.assessment.dto.AssessmentDTO;
import com.medilabo.assessment.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for diabetes risk assessment.
 *
 * GET /api/assessment/{patId}  — assess a patient's diabetes risk level
 */
@Slf4j
@RestController
@RequestMapping("/api/assessment")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;

    /** Assess diabetes risk for the given patient */
    @GetMapping("/{patId}")
    public ResponseEntity<AssessmentDTO> assess(@PathVariable Long patId) {
        log.info("GET /api/assessment/{}", patId);
        AssessmentDTO result = assessmentService.assess(patId);
        log.info("GET /api/assessment/{} — riskLevel={}", patId, result.getRiskLevel());
        return ResponseEntity.ok(result);
    }
}
