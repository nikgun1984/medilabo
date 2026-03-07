package com.medilabo.demographics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
public class PatientDTO {
    private Long id;
    private String lastName;
    private String firstName;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String phone;
    private OffsetDateTime createdAt;
}
