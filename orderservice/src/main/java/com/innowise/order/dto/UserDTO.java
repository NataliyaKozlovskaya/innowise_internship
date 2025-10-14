package com.innowise.order.dto;

import java.time.LocalDate;

/**
 * Data Transfer Object representing a User for API responses
 */
public record UserDTO(
    String name,
    String surname,
    LocalDate birthDate,
    String email
) {

}