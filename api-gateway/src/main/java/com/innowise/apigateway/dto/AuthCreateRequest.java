package com.innowise.apigateway.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthCreateRequest(@NotBlank String uuid,
                                @NotBlank String login,
                                @NotBlank String password
) {

}
