package com.innowise.apigateway.dto.auth.token;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record TokenValidationResponse(
    boolean valid,
    @NotBlank String username,
    @NotBlank List<String> authorities
) {

}