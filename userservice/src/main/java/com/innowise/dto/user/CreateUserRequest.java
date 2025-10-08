package com.java.project.userservice.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

public record CreateUserRequest(
    @NotBlank(message = "Name is required") String name,
    @NotBlank(message = "Surname is required") String surname,
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid") String email,
    @Past(message = "Birth date must be in the past") LocalDate birthDate
) {

}