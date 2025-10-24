package com.innowise.order.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Data Transfer Object representing a User for API responses
 */
public record UserDTO(
    @NotBlank String name,
    @NotBlank String surname,
    @NotNull LocalDate birthDate,
    @Email String email
) {

}