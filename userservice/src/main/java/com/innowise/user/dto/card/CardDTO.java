package com.innowise.user.dto.card;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CardDTO(
    @NotBlank String number,
    @NotBlank String holder,

    @JsonProperty("expiration_date")
    @JsonFormat(pattern = "dd/MM/yyyy")
    LocalDate expirationDate
) {

}
