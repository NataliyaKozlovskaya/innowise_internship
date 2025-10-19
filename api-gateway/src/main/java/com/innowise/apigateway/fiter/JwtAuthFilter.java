package com.innowise.apigateway.fiter;

import com.innowise.apigateway.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

  private final JwtUtil jwtUtil;

  public JwtAuthFilter(JwtUtil jwtUtil) {
    super(Config.class);
    this.jwtUtil = jwtUtil;
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      String path = exchange.getRequest().getPath().value();

      // Пропускаем публичные endpoints
      if (isPublicEndpoint(path)) {
        return chain.filter(exchange);
      }

      // Проверяем JWT токен
      String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
      }

      String token = authHeader.substring(7);

      if (!jwtUtil.validateToken(token)) {
        return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
      }

      // Добавляем user info в headers для downstream сервисов
      String userId = jwtUtil.extractUserId(token);
      String username = jwtUtil.extractUsername(token);

      var modifiedRequest = exchange.getRequest().mutate()
          .header("X-User-Id", userId)
          .header("X-Username", username)
          .build();

      return chain.filter(exchange.mutate().request(modifiedRequest).build());
    };
  }

  private boolean isPublicEndpoint(String path) {
    return path.startsWith("/auth/login") ||
        path.startsWith("/auth/register") ||
        path.startsWith("/auth/validate") ||
        path.equals("/health") ||
        path.startsWith("/actuator");
  }

  private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
    exchange.getResponse().setStatusCode(status);
    return exchange.getResponse().setComplete();
  }

  public static class Config {
    // Конфигурация
  }
}