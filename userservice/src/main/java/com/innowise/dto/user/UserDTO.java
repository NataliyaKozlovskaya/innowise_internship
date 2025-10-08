package com.java.project.userservice.dto.user;

import java.time.LocalDate;

public record UserDTO(
    String name,
    String surname,
    LocalDate birthDate,
    String email
) {

}