package com.java.project.dto.user;

import jakarta.validation.constraints.Email;

public record UpdateUserRequest(
    String name,
    String surname,
    @Email String email
) {

}