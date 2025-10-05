package com.java.project.dto.security;

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