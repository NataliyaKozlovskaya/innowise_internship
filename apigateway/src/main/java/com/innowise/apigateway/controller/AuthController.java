package com.innowise.apigateway.controller;

import com.innowise.apigateway.dto.auth.login.LoginRequest;
import com.innowise.apigateway.dto.auth.login.LoginResponse;
import com.innowise.apigateway.dto.auth.registration.RegistrationRequest;
import com.innowise.apigateway.dto.auth.registration.RegistrationResponse;
import com.innowise.apigateway.dto.auth.token.RefreshTokenRequest;
import com.innowise.apigateway.dto.auth.token.TokenValidationResponse;
import com.innowise.apigateway.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public Mono<ResponseEntity<RegistrationResponse>> register(
      @RequestBody RegistrationRequest request) {
    return authService.registerUser(request)
        .map(user -> ResponseEntity.status(HttpStatus.CREATED).body(user))
        .onErrorResume(error -> {

          log.error("Registration failed for user {}: {}", request.login(), error.getMessage());

          return Mono.just(ResponseEntity.badRequest()
              .body(new RegistrationResponse(request.login(), request.email())));
        });
  }

  @PostMapping("/login")
  public Mono<ResponseEntity<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
    return authService.loginUser(loginRequest)
        .map(ResponseEntity::ok)
        .onErrorResume(error -> {

          log.error("Login failed for user {}: {}", loginRequest.login(), error.getMessage());

          return Mono.just(ResponseEntity.badRequest()
              .body(null));
        });
  }

  @PostMapping("/refresh")
  public Mono<ResponseEntity<LoginResponse>> refreshToken(
      @Valid @RequestBody RefreshTokenRequest request) {
    return authService.refreshToken(request)
        .map(ResponseEntity::ok)
        .onErrorResume(error -> {

          log.error("Refresh token failed ", error.getMessage());

          return Mono.just(ResponseEntity.badRequest()
              .body(null));
        });
  }

  @PostMapping("/validate")
  public Mono<ResponseEntity<TokenValidationResponse>> validateToken(@RequestParam String token) {
    return authService.validateToken(token)
        .map(ResponseEntity::ok)
        .onErrorResume(error -> {

          log.error("Validation of token failed ", error.getMessage());

          return Mono.just(ResponseEntity.badRequest()
              .body(null));
        });
  }
}

