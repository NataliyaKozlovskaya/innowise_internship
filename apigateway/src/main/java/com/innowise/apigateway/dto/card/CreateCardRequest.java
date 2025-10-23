package com.innowise.apigateway.dto.card;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateCardRequest(
    @NotBlank String number,
    @NotBlank String holder,
    @JsonFormat(pattern = "dd/MM/yyyy")
    @JsonProperty("expiration_date")
    @NotNull
    LocalDate expirationDate
) {

}