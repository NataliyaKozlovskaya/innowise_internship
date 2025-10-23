package com.innowise.apigateway.dto.auth.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record RegistrationRequest(@NotBlank String login,
                                  @NotBlank String password,
                                  @NotBlank String name,
                                  @NotBlank String surname,
                                  @NotNull LocalDate birthDate,
                                  @Email String email) {

}