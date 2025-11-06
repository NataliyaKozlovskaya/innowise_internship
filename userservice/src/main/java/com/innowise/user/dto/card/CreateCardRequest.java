package com.innowise.user.dto.card;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * Record representing a card creation request dto
 */
public record CreateCardRequest(
    @NotBlank(message = "Card number is required") String number,
    @NotBlank(message = "Holder is required") String holder,

    @JsonProperty("expiration_date")
    @JsonFormat(pattern = "dd/MM/yyyy")
    LocalDate expirationDate
) {

}