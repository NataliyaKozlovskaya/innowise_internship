package com.innowise.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTO representing a user's role information
 */
public record UserRoleDTO(
    @NotBlank String userLogin,
    @NotBlank String role,
    @NotNull LocalDateTime createdAt
) {

}
