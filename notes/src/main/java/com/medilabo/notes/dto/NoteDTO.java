package com.medilabo.notes.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/** Read model returned by the API */
@Data
@Builder
public class NoteDTO {
    private String id;
    private Long patId;
    private String patient;
    private String note;
    private Instant createdAt;
    private Instant updatedAt;
}
