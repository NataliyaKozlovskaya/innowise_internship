package com.innowise.apigateway.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRegistrationResponse(@NotBlank String login) {}