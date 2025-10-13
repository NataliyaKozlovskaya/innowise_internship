package com.innowise.user.dto.user;

import jakarta.validation.constraints.Email;

public record UpdateUserRequest(
    String name,
    String surname,
    @Email String email
) {

}