package com.innowise.apigateway.dto.login;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank String login,
    @NotBlank String password
) {

}