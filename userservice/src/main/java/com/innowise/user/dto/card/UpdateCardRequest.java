package com.innowise.user.dto.card;

import jakarta.validation.constraints.NotBlank;

public record UpdateCardRequest(
    @NotBlank String holder
) {

}