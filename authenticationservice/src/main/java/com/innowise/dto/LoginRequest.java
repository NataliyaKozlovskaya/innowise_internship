package com.java.project.authenticationservice.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank String login,
    @NotBlank  String password
) {

}