package com.innowise.apigateway.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record UserCreateRequest(@NotBlank String uuid,
                                @NotBlank String name,
                                @NotBlank String surname,
                                @NotBlank LocalDate birthDate,
                                @NotBlank String email) {

}
