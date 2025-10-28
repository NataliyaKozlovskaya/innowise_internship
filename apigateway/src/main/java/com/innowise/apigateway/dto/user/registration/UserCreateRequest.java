package com.innowise.apigateway.dto.user.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Record representing a user creation request data transfer object
 */
public record UserCreateRequest(@NotBlank String uuid,
                                @NotBlank String name,
                                @NotBlank String surname,
                                @NotNull LocalDate birthDate,
                                @Email String email) {

}
