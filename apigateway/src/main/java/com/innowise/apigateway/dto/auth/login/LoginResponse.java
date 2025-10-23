package com.innowise.apigateway.dto.auth.login;

import jakarta.validation.constraints.NotBlank;

public record LoginResponse(
    @NotBlank String accessToken,
    @NotBlank String refreshToken
) {

//  public LoginResponse {
//    if (tokenType == null) {
//      tokenType = "Bearer";
//    }
//  }

//  public LoginResponse(String accessToken, String refreshToken) {
//    this(accessToken, refreshToken, "Bearer");
//  }
}
