package com.innowise.user.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Record representing a user creation response dto
 */
public record UserCreateResponse(
    @NotBlank String uuid,
    @NotBlank String name,
    @NotBlank String surname,
    @NotNull LocalDate birthDate,
    @Email String email
) {

}