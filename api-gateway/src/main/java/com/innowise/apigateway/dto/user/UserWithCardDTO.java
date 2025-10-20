package com.innowise.apigateway.dto.user;

import com.innowise.apigateway.dto.card.CardDTO;
import java.time.LocalDate;
import java.util.List;

public record UserWithCardDTO(String name,
                              String surname,
                              LocalDate birthDate,
                              String email,
                              List<CardDTO> cardDTOList
) {

}
