package com.innowise.apigateway.dto.card;

import jakarta.validation.constraints.NotBlank;

/**
 * Record representing a card update request dto
 */
public record UpdateCardRequest(
    @NotBlank String holder
) {

}