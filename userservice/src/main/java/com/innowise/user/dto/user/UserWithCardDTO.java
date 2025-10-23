package com.innowise.user.dto.user;

import com.innowise.user.dto.card.CardDTO;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

public record UserWithCardDTO(
    @NotBlank(message = "Name is required") String name,
                              String surname,
                              LocalDate birthDate,
                              String email,
                              List<CardDTO> cardDTOList
) {

}
