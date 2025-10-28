package com.innowise.apigateway.dto.auth.login;

import jakarta.validation.constraints.NotBlank;

/**
 * Record representing a login response dto
 */
public record LoginResponse(
    @NotBlank String accessToken,
    @NotBlank String refreshToken
) {

}
