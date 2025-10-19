package com.innowise.authentication.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRegistrationResponse(@NotBlank String uuid) {

}
