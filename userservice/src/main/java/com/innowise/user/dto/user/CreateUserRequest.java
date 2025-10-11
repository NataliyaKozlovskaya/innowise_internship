package com.innowise.user.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

public record CreateUserRequest(
    @NotBlank(message = "Id is required") String uuid,
    @NotBlank(message = "Name is required") String name,
    @NotBlank(message = "Surname is required") String surname,
    @Past(message = "Birth date must be in the past") LocalDate birthDate,
    @Email(message = "Email should be valid") String email
) {

}