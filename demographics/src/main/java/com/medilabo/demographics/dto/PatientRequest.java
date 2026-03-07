package com.medilabo.demographics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientRequest {

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required")
    @Size(min = 1, max = 1, message = "Gender must be a single character (M or F)")
    @Pattern(regexp = "[MF]", message = "Gender must be M or F")
    private String gender;

    // optional
    private String address;

    // optional
    private String phone;
}
