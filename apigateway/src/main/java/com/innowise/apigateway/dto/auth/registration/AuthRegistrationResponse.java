package com.innowise.apigateway.dto.auth.registration;

import jakarta.validation.constraints.NotBlank;

public record AuthRegistrationResponse(@NotBlank String login) {}