package com.innowise.apigateway.controller;

import com.innowise.apigateway.dto.auth.login.LoginRequest;
import com.innowise.apigateway.dto.auth.registration.RegistrationRequest;
import com.innowise.apigateway.dto.auth.registration.RegistrationResponse;
import com.innowise.apigateway.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Spring WebFlux controller handling authentication-related HTTP endpoints
 */
@Slf4j
@Component
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  public Mono<ServerResponse> register(ServerRequest request) {
    return request.bodyToMono(RegistrationRequest.class)
        .flatMap(authService::registerUser)
        .flatMap(user -> ServerResponse.status(HttpStatus.CREATED).bodyValue(user))
        .onErrorResume(error -> {

          log.error("Registration failed: {}", error.getMessage());

          return ServerResponse.badRequest()
              .bodyValue(new RegistrationResponse("error", "error@email.com"));
        });
  }

  public Mono<ServerResponse> login(ServerRequest request) {
    return request.bodyToMono(LoginRequest.class)
        .flatMap(authService::loginUser)
        .flatMap(body -> ServerResponse.status(HttpStatus.OK).bodyValue(body))
        .onErrorResume(error -> {
          log.error("Login failed for user ", error.getMessage());
          return ServerResponse.badRequest().build();
        });
  }

  public Mono<ServerResponse> refreshToken(ServerRequest request) {
    return request.bodyToMono(LoginRequest.class)
        .flatMap(authService::loginUser)
        .flatMap(body -> ServerResponse.status(HttpStatus.OK).bodyValue(body))
        .onErrorResume(error -> {
          log.error("Refresh token failed ", error.getMessage());
          return ServerResponse.badRequest().build();
        });
  }

  public Mono<ServerResponse> validateToken(ServerRequest request) {
    return Mono.fromCallable(() -> request.queryParam("token")
            .orElseThrow(() -> new IllegalArgumentException("Token parameter is required")))
        .flatMap(authService::validateToken)
        .flatMap(response -> ServerResponse.ok().bodyValue(response))
        .onErrorResume(error -> {
          log.error("Validation of token failed ", error);
          return ServerResponse.badRequest().build();
        });
  }
}

