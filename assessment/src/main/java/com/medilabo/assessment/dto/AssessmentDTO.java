package com.medilabo.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for the risk assessment endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentDTO {
    private Long patId;
    private String patientFullName;
    private int age;
    private String gender;
    private String riskLevel;          // None, Borderline, InDanger, EarlyOnset
    private int triggerCount;
    private List<String> triggersFound;
}
