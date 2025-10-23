package com.innowise.apigateway.dto.auth.token;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record TokenValidationResponse(
    boolean valid,
    @NotBlank String username,
    @NotBlank List<String> authorities
) {

  public TokenValidationResponse withUsername(String username) {
    return new TokenValidationResponse(this.valid, username, this.authorities);
  }

  public TokenValidationResponse withAuthorities(List<String> authorities) {
    return new TokenValidationResponse(this.valid, this.username, authorities);
  }
}