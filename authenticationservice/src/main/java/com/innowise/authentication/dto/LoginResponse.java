package com.innowise.authentication.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginResponse(
    @NotBlank String accessToken,
    @NotBlank String refreshToken
//    ,
//    String tokenType
) {

//  public LoginResponse {
//    if (tokenType == null) {
//      tokenType = "Bearer";
//    }
//  }
//
//  public LoginResponse(String accessToken, String refreshToken) {
//    this(accessToken, refreshToken, "Bearer");
//  }
}