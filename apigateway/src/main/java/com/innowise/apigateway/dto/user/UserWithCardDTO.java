package com.innowise.apigateway.dto.user;

import com.innowise.apigateway.dto.card.CardDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record UserWithCardDTO(String name,
                              @NotBlank String surname,
                              @NotNull LocalDate birthDate,
                              @Email String email,
                              List<CardDTO> cardDTOList
) {

}
