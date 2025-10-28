package com.innowise.apigateway.filter;

import com.innowise.apigateway.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class JwtRouterFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

  private final AuthService authService;

  public JwtRouterFilter(AuthService authService) {
    this.authService = authService;
  }

  @Override
  public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
    String path = request.path();

    if (path.equals("/api/v1/auth/register")
        || path.equals("/api/v1/auth/login")
        || path.equals("/api/v1/users/register")) {
      return next.handle(request);
    }

    String token = extractToken(request);
    if (token == null) {
      return ServerResponse.status(HttpStatus.UNAUTHORIZED)
          .bodyValue("Missing JWT token");
    }

    return authService.validateToken(token)
        .flatMap(valid -> {
          if (valid.valid()) {
            return next.handle(request);
          } else {
            return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                .bodyValue("Invalid JWT token");
          }
        });
  }

  private String extractToken(ServerRequest request) {
    String authHeader = request.headers().firstHeader("Authorization");
    return (authHeader != null && authHeader.startsWith("Bearer "))
        ? authHeader.substring(7) : null;
  }
}
