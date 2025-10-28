package com.innowise.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * Record representing a user creation request dto
 */
public record UserCreateRequest(
    @NotBlank String uuid,
    @NotBlank String name,
    @NotBlank String surname,
    @NotBlank LocalDate birthDate,
    @NotBlank String email
) {

}
