package com.innowise.authentication.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Record representing a refresh token request dto
 */
public record RefreshTokenRequest(
    @NotBlank String refreshToken
) {

}