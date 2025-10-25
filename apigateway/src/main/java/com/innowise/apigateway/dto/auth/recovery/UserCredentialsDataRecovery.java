package com.innowise.apigateway.dto.auth.recovery;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public record UserCredentialsDataRecovery(
    @NotBlank String uuid,
    @NotBlank String passwordHash,
    @NotBlank String login,
    @NotNull LocalDateTime createdAt,
    @NotNull LocalDateTime updatedAt,
    @NotNull List<UserRoleDTO> roles

) {

}
