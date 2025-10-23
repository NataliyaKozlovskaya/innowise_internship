package com.innowise.apigateway.dto.user.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserCreateResponse(@NotBlank String uuid,
                                 @NotBlank String name,
                                 @NotBlank String surname,
                                 @Email String email) {

}