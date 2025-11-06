package com.innowise.apigateway.filter;

import com.innowise.apigateway.manager.AuthOperationManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Router filter component for JWT authentication. Intercepts incoming HTTP requests and validates
 * JWT tokens for securing API endpoints.
 */
@Component
public class JwtRouterFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

  private final AuthOperationManager authOperationManager;

  public JwtRouterFilter(AuthOperationManager authOperationManager) {
    this.authOperationManager = authOperationManager;
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

    return authOperationManager.validateToken(token)
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
