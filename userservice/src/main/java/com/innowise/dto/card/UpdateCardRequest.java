package com.java.project.userservice.dto.card;

import jakarta.validation.constraints.NotBlank;

public record UpdateCardRequest(
    @NotBlank String holder
) {

}