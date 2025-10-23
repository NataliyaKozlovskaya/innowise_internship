package com.innowise.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtHelper {

  public boolean validateToken(String token) {
    if (token == null || token.isEmpty()) {
      log.debug("Token is null or empty");
      return false;
    }

    try {
      // Basic JWT format validation (3 parts separated by dots)
      String[] parts = token.split("\\.");
      if (parts.length != 3) {
        log.debug("Invalid JWT format - expected 3 parts, got {}", parts.length);
        return false;
      }

      // Decode the payload for verification
      String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
      log.debug("JWT payload: {}", payload);

      return true;

    } catch (Exception e) {
      log.debug("Token validation failed: {}", e.getMessage());
      return false;
    }
  }

  public String extractUserId(String token) {
    try {
      String[] parts = token.split("\\.");
      String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));

      com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
      com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(payload);

      if (jsonNode.has("sub")) {
        return jsonNode.get("sub").asText();
      }

      return "unknown-user";

    } catch (Exception e) {
      log.warn("Failed to extract user ID from token: {}", e.getMessage());
      return "unknown-user";
    }
  }

  public String extractUsername(String token) {
    try {
      String[] parts = token.split("\\.");
      String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));

      com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
      com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(payload);

      if (jsonNode.has("username")) {
        return jsonNode.get("username").asText();
      }
      if (jsonNode.has("preferred_username")) {
        return jsonNode.get("preferred_username").asText();
      }

      return extractUserId(token);

    } catch (Exception e) {
      log.warn("Failed to extract username from token: {}", e.getMessage());
      return "unknown-user";
    }
  }
}