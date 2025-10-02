package com.java.project.dto.card;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CreateCardRequest(
    @NotBlank(message = "Card number is required") String number,
    @NotBlank(message = "Holder is required") String holder,
    LocalDate expirationDate
) {

}