package com.innowise.user.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UserCreateResponse(
    @NotBlank String uuid,
    @NotBlank String name,
    @NotBlank String surname,
    @NotNull LocalDate birthDate,
    @Email String email
) {

}