package com.innowise.authentication.dto;

public record LoginResponse(
    String accessToken,
    String refreshToken,
    String tokenType
) {

  public LoginResponse {
    if (tokenType == null) {
      tokenType = "Bearer";
    }
  }

  public LoginResponse(String accessToken, String refreshToken) {
    this(accessToken, refreshToken, "Bearer");
  }
}