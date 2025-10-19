package com.innowise.apigateway.dto;

import jakarta.validation.constraints.NotBlank;

public record RegistrationResponse(@NotBlank String login, @NotBlank String email) {

}