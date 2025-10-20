package com.innowise.apigateway.dto.auth.token;

import java.util.List;

public record TokenValidationResponse(
    boolean valid,
    String username,
    List<String> authorities
) {

  public TokenValidationResponse withUsername(String username) {
    return new TokenValidationResponse(this.valid, username, this.authorities);
  }

  public TokenValidationResponse withAuthorities(List<String> authorities) {
    return new TokenValidationResponse(this.valid, this.username, authorities);
  }
}