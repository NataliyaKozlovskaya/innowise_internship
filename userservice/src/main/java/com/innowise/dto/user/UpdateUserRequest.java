package com.java.project.userservice.dto.user;

import jakarta.validation.constraints.Email;

public record UpdateUserRequest(
    String name,
    String surname,
    @Email String email
) {

}