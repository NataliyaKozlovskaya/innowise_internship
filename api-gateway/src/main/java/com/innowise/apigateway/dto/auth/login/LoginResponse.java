package com.innowise.apigateway.dto.auth.login;

public record LoginResponse(
    String accessToken,
    String refreshToken
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
