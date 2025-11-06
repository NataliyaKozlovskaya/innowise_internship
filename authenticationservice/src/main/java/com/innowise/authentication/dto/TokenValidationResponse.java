package com.innowise.authentication.dto;

import java.util.List;

/**
 * Record representing a token validation response dto
 */
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