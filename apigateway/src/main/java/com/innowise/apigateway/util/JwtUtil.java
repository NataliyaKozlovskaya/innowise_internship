package com.innowise.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtUtil {

  @Value("${jwt.secret}")
  private String secret;

  /**
   * Проверяет валидность JWT токена
   */
  public Mono<Boolean> validateToken(String token) {
    return Mono.fromCallable(() -> {
      try {
        Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
            .build()
            .parseClaimsJws(token);
        return true;
      } catch (Exception e) {
        log.warn("JWT validation failed: {}", e.getMessage());
        return false;
      }
    }).onErrorReturn(false);
  }

  /**
   * Извлекает username (subject) из JWT токена
   */
  public Mono<String> extractUsername(String token) {
    return Mono.fromCallable(() -> {
      Claims claims = Jwts.parserBuilder()
          .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
          .build()
          .parseClaimsJws(token)
          .getBody();
      return claims.getSubject();
    });
  }
}