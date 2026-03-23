package com.medilabo.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO matching the response from the Notes microservice.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteDTO {
    private String id;
    private Long patId;
    private String patient;
    private String note;
    private String createdAt;
    private String updatedAt;
}
