package com.innowise.apigateway.dto.card;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO representing a payment card entity in the system
 */
public record CardDTO(
    @NotBlank String number,
    @NotBlank String holder,
    @JsonFormat(pattern = "dd/MM/yyyy")
    @JsonProperty("expiration_date")
    @NotNull
    LocalDate expirationDate
) {

}
