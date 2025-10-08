package com.java.project.userservice.dto.card;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CardDTO(
    @NotBlank String number,
    @NotBlank String holder,
    LocalDate expirationDate
) {

}
