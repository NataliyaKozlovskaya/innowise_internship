package com.innowise.authentication.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Record representing a login request dto
 */
public record LoginRequest(
    @NotBlank String login,
    @NotBlank String password
) {

}