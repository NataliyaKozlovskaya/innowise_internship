package com.innowise.authentication.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Component for handling JWT operations including generation, validation and parsing of tokens
 */
@Component
@Slf4j
public class JwtTokenProvider {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.access-token.expiration:3600000}")
  private long accessTokenExpiration;

  @Value("${jwt.refresh-token.expiration:86400000}")
  private long refreshTokenExpiration;

  /**
   * Generates a signing key from the configured JWT secret
   *
   * @return SecretKey for signing and verifying JWT tokens
   */
  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Generates an access token for the specified user with granted authorities
   *
   * @param username    the subject (username) of the token
   * @param authorities list of granted authorities/roles for the user
   * @return signed JWT access token as a string
   */
  public String generateAccessToken(String username, List<String> authorities) {
    return Jwts.builder()
        .subject(username)
        .claim("authorities", authorities)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
        .signWith(getSigningKey())
        .compact();
  }

  /**
   * Generates a refresh token for the specified user
   *
   * @param username the subject (username) of the token
   * @return signed JWT refresh token as a string
   */
  public String generateRefreshToken(String username) {
    return Jwts.builder()
        .subject(username)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
        .signWith(getSigningKey())
        .compact();
  }

  /**
   * Validates the integrity and expiration of a JWT token
   *
   * @param token the JWT token to validate
   * @return true if the token is valid and not expired, false otherwise
   */
  public boolean validateToken(String token) {
    try {
      Jwts.parser()
          .verifyWith(getSigningKey())
          .build()
          .parseSignedClaims(token);
      return true;
    } catch (JwtException ex) {
      log.error("Token validation error: {}", ex.getMessage());
      return false;
    }
  }

  /**
   * Extracts the username (subject) from a JWT token
   *
   * @param token the JWT token to parse
   * @return username extracted from the token subject
   * @throws JwtException if the token is invalid or cannot be parsed
   */
  public String getUsernameFromToken(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }

  /**
   * Checks if the refresh token is expiring within the threshold period
   *
   * @param refreshToken the refresh token to check
   * @return true if the token is expiring soon, false otherwise
   */
  public boolean isRefreshTokenExpiringSoon(String refreshToken) {
    try {
      Date expiration = getExpirationDateFromToken(refreshToken);
      Instant expirationInstant = expiration.toInstant();
      Instant now = Instant.now();

      Duration timeUntilExpiration = Duration.between(now, expirationInstant);
      Duration threshold = Duration.ofDays(7);

      return timeUntilExpiration.compareTo(threshold) <= 0;
    } catch (Exception e) {
      log.warn("Failed to check refresh token expiration: {}", e.getMessage());
      return false;
    }
  }

  /**
   * Extracts the expiration date from a JWT token.
   *
   * @param token the JWT token to parse
   * @return expiration date of the token
   * @throws JwtException if the token is invalid or cannot be parsed
   */
  private Date getExpirationDateFromToken(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();

    return claims.getExpiration();
  }

  /**
   * Extracts the list of authorities from a JWT token.
   *
   * @param token the JWT token to parse
   * @return list of authorities granted to the token subject
   * @throws JwtException if the token is invalid or cannot be parsed
   */
  @SuppressWarnings("unchecked")
  public List<String> getAuthoritiesFromToken(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .get("authorities", List.class);
  }
}