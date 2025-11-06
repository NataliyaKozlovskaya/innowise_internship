package com.innowise.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * Record representing an authentication creation request dto
 */
public record AuthCreateRequest(
    @NotBlank String uuid,
    @NotBlank String password,
    @NotBlank String login
) {

}