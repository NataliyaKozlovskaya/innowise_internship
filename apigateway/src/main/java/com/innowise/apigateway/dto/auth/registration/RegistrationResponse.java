package com.innowise.apigateway.dto.auth.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Record representing a user registration response dto
 */
public record RegistrationResponse(@NotBlank String login, @Email String email) {

}