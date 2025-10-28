package com.innowise.authentication.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Record representing a login response dto
 */
public record LoginResponse(
    @NotBlank String accessToken,
    @NotBlank String refreshToken
) {
}