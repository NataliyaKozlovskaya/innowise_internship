package com.innowise.apigateway.dto.user.registration;

import jakarta.validation.constraints.NotBlank;

public record UserResponse(@NotBlank String uuid,
                           @NotBlank String name,
                           @NotBlank String surname,
                           @NotBlank String email) {

}