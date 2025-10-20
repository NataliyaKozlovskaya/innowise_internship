package com.innowise.user.dto.user;

import com.innowise.user.dto.card.CardDTO;
import java.time.LocalDate;
import java.util.List;

public record UserWithCardDTO(String name,
                              String surname,
                              LocalDate birthDate,
                              String email,
                              List<CardDTO> cardDTOList
) {

}
