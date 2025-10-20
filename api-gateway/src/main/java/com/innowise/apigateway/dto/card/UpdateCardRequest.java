package com.innowise.apigateway.dto.card;

import jakarta.validation.constraints.NotBlank;

public record UpdateCardRequest(
    @NotBlank String holder
) {

}