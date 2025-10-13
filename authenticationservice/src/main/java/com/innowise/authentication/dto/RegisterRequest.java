package com.innowise.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record RegisterRequest(
    @NotBlank String login,
    @NotBlank String password,
    @NotBlank String name,
    @NotBlank String surname,
    LocalDate birthDate,
    @NotBlank String email
) {

}