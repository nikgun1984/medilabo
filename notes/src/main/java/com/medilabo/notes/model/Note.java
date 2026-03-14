package com.medilabo.notes.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document representing a physician's note for a patient visit.
 * Original formatting (line breaks, etc.) is preserved as-is in the {@code note} field.
 */
@Document(collection = "notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Note {

    @Id
    private String id;

    /** Foreign key to the patient in the demographics service */
    @Indexed
    private Long patId;

    /** Patient last name — denormalised for quick display */
    private String patient;

    /** Free-text note; no length limit, original formatting preserved */
    private String note;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
