package com.medilabo.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO matching the response from the Demographics microservice.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientDTO {
    private Long id;
    private String lastName;
    private String firstName;
    private LocalDate dateOfBirth;
    private String gender;   // "M" or "F"
    private String address;
    private String phone;
}
