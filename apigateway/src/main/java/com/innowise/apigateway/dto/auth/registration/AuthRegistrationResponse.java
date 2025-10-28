package com.innowise.apigateway.dto.auth.registration;

import jakarta.validation.constraints.NotBlank;

/**
 * Record representing an authentication registration response dto
 */
public record AuthRegistrationResponse(@NotBlank String login) {

}