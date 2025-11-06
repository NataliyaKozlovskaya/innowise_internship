package com.innowise.user.dto.user;

import com.innowise.user.dto.card.CardDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO representing a user with their associated cards
 */
public record UserWithCardDTO(
    @NotBlank String name,
    @NotBlank String surname,
    @NotNull LocalDate birthDate,
    @Email String email,
    List<CardDTO> cardDTOList
) {

}
