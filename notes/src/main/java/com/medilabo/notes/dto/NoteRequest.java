package com.medilabo.notes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/** Write model accepted by POST and PUT endpoints */
@Data
public class NoteRequest {

    @NotNull(message = "patId is required")
    @Positive(message = "patId must be a positive number")
    private Long patId;

    @NotBlank(message = "patient name is required")
    private String patient;

    /**
     * Free-text physician note.
     * No length constraint — original formatting (line breaks etc.) is preserved.
     */
    @NotBlank(message = "note content is required")
    private String note;
}
