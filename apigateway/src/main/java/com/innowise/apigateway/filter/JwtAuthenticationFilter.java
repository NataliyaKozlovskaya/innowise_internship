package com.innowise.apigateway.filter;

import com.innowise.apigateway.filter.JwtAuthenticationFilter.Config;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequest.Builder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<Config> {

  private final JwtHelper jwtHelper;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  public JwtAuthenticationFilter(JwtHelper jwtHelper) {
    super(Config.class);
    this.jwtHelper = jwtHelper;
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      String path = exchange.getRequest().getPath().value();

      // Skipping public endpoints from the configuration
      if (config.getPublicEndpoints().stream()
          .anyMatch(pattern -> pathMatcher.match(pattern, path))) {
        return chain.filter(exchange);
      }

      // Verifying the token for secure endpoints
      String token = extractToken(exchange);

      if (token == null) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
      }

      if (!jwtHelper.validateToken(token)) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
      }

      // Adding user data to headers
      String userId = jwtHelper.extractUserId(token);
      ServerHttpRequest mutatedRequest = (ServerHttpRequest) exchange.getRequest().mutate()
          .header("X-User-Id", userId)
          .build();

      return chain.filter(exchange.mutate().request((Consumer<Builder>) mutatedRequest).build());
    };
  }

  private String extractToken(ServerWebExchange exchange) {
    String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return null;
  }

  public static class Config {
    private List<String> publicEndpoints = new ArrayList<>();

    public List<String> getPublicEndpoints() {
      return publicEndpoints;
    }

    public void setPublicEndpoints(List<String> publicEndpoints) {
      this.publicEndpoints = publicEndpoints;
    }
  }
}