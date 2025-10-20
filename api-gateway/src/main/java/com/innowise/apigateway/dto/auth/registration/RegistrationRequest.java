package com.innowise.apigateway.dto.auth.registration;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record RegistrationRequest(@NotBlank String login,
                                  @NotBlank String password,
                                  @NotBlank String name,
                                  @NotBlank String surname,
                                  LocalDate birthDate,
                                  @NotBlank String email) {

}