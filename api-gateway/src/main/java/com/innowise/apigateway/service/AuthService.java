package com.innowise.apigateway.service;

import com.innowise.apigateway.config.ServiceConfig;
import com.innowise.apigateway.dto.auth.login.LoginRequest;
import com.innowise.apigateway.dto.auth.login.LoginResponse;
import com.innowise.apigateway.dto.auth.registration.AuthCreateRequest;
import com.innowise.apigateway.dto.auth.registration.AuthRegistrationResponse;
import com.innowise.apigateway.dto.auth.registration.RegistrationRequest;
import com.innowise.apigateway.dto.auth.registration.RegistrationResponse;
import com.innowise.apigateway.dto.auth.token.RefreshTokenRequest;
import com.innowise.apigateway.dto.auth.token.TokenValidationResponse;
import com.innowise.apigateway.dto.user.registration.UserCreateRequest;
import com.innowise.apigateway.dto.user.registration.UserResponse;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class AuthService {

  private final WebClient webClient;
  private final ServiceConfig serviceConfig;

  public AuthService(Builder webClientBuilder, ServiceConfig serviceConfig) {
    this.webClient = webClientBuilder.build();
    this.serviceConfig = serviceConfig;
  }

  public Mono<RegistrationResponse> registerUser(RegistrationRequest request) {
    log.info("API Gateway: Starting registration for user: {}", request.email());

    return createUserInUserService(request)
        .flatMap(userResponse -> {
          String userId = userResponse.uuid();
          log.info("User created successfully: {}, ID: {}", userResponse.email(), userId);

          return createCredentialsInAuthService(request, userId)
              .thenReturn(new RegistrationResponse(
                  request.login(),
                  request.email()))
              .onErrorResume(error ->
                  rollbackUser(userId, request.login())
                      .then(Mono.error(
                          new RuntimeException("Registration failed in auth service", error)))
              );
        })
        .onErrorResume(error -> {
          log.error("User creation failed for {}: {}", request.login(), error.getMessage());
          return Mono.error(new RuntimeException("Registration failed in user service", error));
        });
  }

  public Mono<LoginResponse> loginUser(LoginRequest loginRequest) {
    log.info("API Gateway: Attempting login for user: {}", loginRequest.login());

    return createLoginUserInAuthService(loginRequest)
        .flatMap(authResponse -> {
          log.info("API Gateway: Login successful for user: {}", loginRequest.login());
          return Mono.just(new LoginResponse(
              authResponse.accessToken(),
              authResponse.refreshToken()
          ));
        })
        .onErrorResume(error -> {
          log.error("API Gateway: Login failed for user {}: {}",
              loginRequest.login(), error.getMessage());
          return Mono.error(new RuntimeException("Login failed", error));
        });
  }

  public Mono<LoginResponse> refreshToken(RefreshTokenRequest request) {
    log.info("API Gateway: Attempting refresh token for user");

    return createRefreshTokenInAuthService(request)
        .flatMap(refreshResponse -> {
          log.info("API Gateway: Refresh token successful");
          return Mono.just(new LoginResponse(
              refreshResponse.accessToken(),
              refreshResponse.refreshToken()
          ));
        })
        .onErrorResume(error -> {
          log.error("API Gateway: Refresh token failed", error.getMessage());
          return Mono.error(new RuntimeException("Refresh token failed", error));
        });
  }

  public Mono<TokenValidationResponse> validateToken(String token) {
    log.info("API Gateway: Attempting validate token");

    return createValidateTokenInAuthService(token)
        .flatMap(validateResponse -> {
          log.info("API Gateway: Validate token successful");
          return Mono.just(new TokenValidationResponse(
              true,
              validateResponse.username(),
              validateResponse.authorities()
          ));
        })
        .onErrorResume(error -> {
          log.error("API Gateway: Validate token failed", error.getMessage());
          return Mono.error(new RuntimeException("Validate token failed", error));
        });
  }

  private Mono<Void> rollbackUser(String userId, String login) {
    log.warn("Starting rollback for user due to auth service failure. UserId: {}, Login: {}",
        userId, login);

    return rollbackUserCreation(userId)
        .doOnSuccess(v ->
            log.info("Rollback completed successfully. User deleted: {}", login)
        )
        .doOnError(rollbackError ->
            log.error("Rollback failed for user {}: {}. Manual cleanup required!",
                login, rollbackError.getMessage())
        )
        .onErrorResume(rollbackError -> {
          log.error("CRITICAL: Failed to delete user during rollback. UserId: {}, Error: {}",
              userId, rollbackError.getMessage());
          return Mono.empty(); // Продолжаем с оригинальной ошибкой
        });
  }

  private Mono<LoginResponse> createRefreshTokenInAuthService(RefreshTokenRequest request) {
    return webClient.post()
        .uri(serviceConfig.getAuthServiceUrl() + "/api/v1/auth/refresh")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(LoginResponse.class)
        .doOnError(
            error -> log.error("Failed to refresh token in Auth Service: {}", error.getMessage()));
  }

  private Mono<TokenValidationResponse> createValidateTokenInAuthService(String token) {
    return webClient.post()
        .uri(serviceConfig.getAuthServiceUrl() + "/api/v1/auth/validate")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(token)
        .retrieve()
        .bodyToMono(TokenValidationResponse.class)
        .doOnError(
            error -> log.error("Failed to validate token in Auth Service: {}", error.getMessage()));
  }

  private Mono<LoginResponse> createLoginUserInAuthService(LoginRequest loginRequest) {
    return webClient.post()
        .uri(serviceConfig.getAuthServiceUrl() + "/api/v1/users/login")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(loginRequest)
        .retrieve()
        .bodyToMono(LoginResponse.class)
        .doOnError(
            error -> log.error("Failed to login user in User Service: {}", error.getMessage()));
  }

  private Mono<UserResponse> createUserInUserService(RegistrationRequest request) {
    UserCreateRequest userRequest = new UserCreateRequest(
        UUID.randomUUID().toString(),
        request.name(),
        request.surname(),
        request.birthDate(),
        request.email());

    return webClient.post()
        .uri(serviceConfig.getUserServiceUrl() + "/api/v1/users")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(userRequest)
        .retrieve()
        .bodyToMono(UserResponse.class)
        .doOnError(
            error -> log.error("Failed to create user in User Service: {}", error.getMessage()));
  }

  private Mono<AuthRegistrationResponse> createCredentialsInAuthService(
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

  private Mono<Void> rollbackUserCreation(String email) {
    log.info("Rolling back user creation for: {}", email);

    return webClient.delete()
        .uri(serviceConfig.getUserServiceUrl() + "/api/v1/users/{id}", email)
        .retrieve()
        .toBodilessEntity()
        .then()
        .onErrorResume(error -> {
          log.warn("User rollback failed for {}: {}. User might not exist.", email,
              error.getMessage());
          return Mono.empty();
        });
  }
}