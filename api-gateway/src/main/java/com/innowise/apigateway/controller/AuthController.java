package com.innowise.apigateway.controller;

import com.innowise.apigateway.dto.RegistrationRequest;
import com.innowise.apigateway.dto.RegistrationResponse;
import com.innowise.apigateway.dto.login.LoginRequest;
import com.innowise.apigateway.dto.login.LoginResponse;
import com.innowise.apigateway.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
        .map(ResponseEntity::ok)
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
              .body(new LoginResponse(null, null)));
        });
  }
}

