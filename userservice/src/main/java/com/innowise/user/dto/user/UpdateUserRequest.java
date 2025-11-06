package com.innowise.user.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Record representing a user update request dto
 */
public record UpdateUserRequest(
    @NotBlank String name,
    @NotBlank String surname,
    @Email String email
) {

}