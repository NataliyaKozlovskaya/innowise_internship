package com.java.project.authenticationservice.dto;

public record TokenResponse(
    String accessToken,
    String refreshToken,
    String tokenType
) {

  public TokenResponse {
    if (tokenType == null) {
      tokenType = "Bearer";
    }
  }

  public TokenResponse(String accessToken, String refreshToken) {
    this(accessToken, refreshToken, "Bearer");
  }
}