package com.innowise.apigateway.service;

import com.innowise.apigateway.config.ServiceConfig;
import com.innowise.apigateway.dto.auth.login.LoginRequest;
import com.innowise.apigateway.dto.auth.login.LoginResponse;
import com.innowise.apigateway.dto.auth.registration.AuthCreateRequest;
import com.innowise.apigateway.dto.auth.registration.AuthRegistrationResponse;
import com.innowise.apigateway.dto.auth.registration.RegistrationRequest;
import com.innowise.apigateway.dto.auth.token.RefreshTokenRequest;
import com.innowise.apigateway.dto.auth.token.TokenValidationResponse;
import com.innowise.apigateway.dto.user.registration.UserCreateRequest;
import com.innowise.apigateway.dto.user.registration.UserCreateResponse;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthServiceClient {

  private final WebClient webClient;
  private final ServiceConfig serviceConfig;

  public AuthServiceClient(WebClient webClient, ServiceConfig serviceConfig) {
    this.webClient = webClient;
    this.serviceConfig = serviceConfig;
  }


  public Mono<LoginResponse> createRefreshTokenInAuthService(RefreshTokenRequest request) {
    return webClient.post()
        .uri(serviceConfig.getAuthServiceUrl() + "/api/v1/auth/refresh")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(LoginResponse.class)
        .doOnError(
            error -> log.error("Failed to refresh token in Auth Service: {}", error.getMessage()));
  }

  public Mono<TokenValidationResponse> createValidateTokenInAuthService(String token) {
    log.info("GATEWAY: Validating token: {}", token != null ?
        token.substring(0, Math.min(10, token.length())) + "..." : "null");
    String fullUrl = serviceConfig.getAuthServiceUrl() + "/api/v1/auth/validate?token=" + token;

    return webClient.post()
        .uri(fullUrl)
        .retrieve()
        .bodyToMono(TokenValidationResponse.class)
        .doOnNext(response ->
            log.info("GATEWAY: Auth service response - valid: {}", response.valid()))
        .doOnError(error ->
            log.error("GATEWAY: Auth service error: {}", error.getMessage()))
        .onErrorReturn(new TokenValidationResponse(false, null, null));
  }

  public Mono<LoginResponse> createLoginUserInAuthService(LoginRequest loginRequest) {
    return webClient.post()
        .uri(serviceConfig.getAuthServiceUrl() + "/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(loginRequest)
        .retrieve()
        .bodyToMono(LoginResponse.class)
        .doOnError(
            error -> log.error("Failed to login user in AuthService: {}", error.getMessage()));
  }


  public Mono<AuthRegistrationResponse> createCredentialsInAuthService(
      RegistrationRequest request, String userId) {

    AuthCreateRequest authRequest = new AuthCreateRequest(
        userId,
        request.login(),
        request.password()
    );

    return webClient.post()
        .uri(serviceConfig.getAuthServiceUrl() + "/api/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(authRequest)
        .retrieve()
        .bodyToMono(AuthRegistrationResponse.class)
        .doOnError(error -> log.error(
            "Failed to create credentials for user with login {} in Auth Service: {}",
            authRequest.login(), error.getMessage()));
  }
}
