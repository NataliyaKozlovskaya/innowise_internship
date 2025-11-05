package com.innowise.apigateway.manager;

import com.innowise.apigateway.dto.auth.login.LoginRequest;
import com.innowise.apigateway.dto.auth.login.LoginResponse;
import com.innowise.apigateway.dto.auth.registration.RegistrationRequest;
import com.innowise.apigateway.dto.auth.registration.RegistrationResponse;
import com.innowise.apigateway.dto.auth.token.RefreshTokenRequest;
import com.innowise.apigateway.dto.auth.token.TokenValidationResponse;
import com.innowise.apigateway.service.AuthServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthOperationManager {

  private final AuthServiceClient authClient;
  private final RollbackManager rollbackManager;

  public AuthOperationManager(AuthServiceClient authClient, RollbackManager rollbackManager) {
    this.authClient = authClient;
    this.rollbackManager = rollbackManager;
  }


  /**
   * User registration
   */
  public Mono<RegistrationResponse> registerUser(RegistrationRequest request) {
    log.info("API Gateway: Starting registration for user: {}", request.email());

    return authClient.createUserInUserService(request)
        .flatMap(userResponse -> {
          String userId = userResponse.uuid();
          log.info("User created successfully: {}, ID: {}", userResponse.email(), userId);

          return authClient.createCredentialsInAuthService(request, userId)
              .thenReturn(new RegistrationResponse(
                  request.login(),
                  request.email()))
              .onErrorResume(error ->
                  rollbackManager.rollbackUser(userId, request.login())
                      .then(Mono.error(
                          new RuntimeException("Registration failed in auth service", error)))
              );
        })
        .onErrorResume(error -> {
          log.error("User creation failed for {}: {}", request.login(), error.getMessage());
          return Mono.error(new RuntimeException("Registration failed in user service", error));
        });
  }

  /**
   * User login
   */
  public Mono<LoginResponse> loginUser(LoginRequest loginRequest) {
    log.info("API Gateway: Attempting login for user: {}", loginRequest.login());

    return authClient.createLoginUserInAuthService(loginRequest)
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

  /**
   * Refresh token
   */
  public Mono<LoginResponse> refreshToken(RefreshTokenRequest request) {
    log.info("API Gateway: Attempting refresh token for user");

    return authClient.createRefreshTokenInAuthService(request)
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

  /**
   * Validate token
   */
  public Mono<TokenValidationResponse> validateToken(String token) {
    log.info("API Gateway: Attempting validate token");

    return authClient.createValidateTokenInAuthService(token)
        .flatMap(validateResponse -> {
          if (validateResponse.valid()) {
            log.info("API Gateway: Validate token successful");
            return Mono.just(new TokenValidationResponse(
                true,
                validateResponse.username(),
                validateResponse.authorities()
            ));
          } else {
            log.info("API Gateway: Validate token failed - invalid token");
            return Mono.just(new TokenValidationResponse(
                false,
                null,
                null
            ));
          }
        })
        .onErrorResume(error -> {
          log.error("API Gateway: Validate token failed: {}", error.getMessage());
          return Mono.just(new TokenValidationResponse(false, null, null));
        });
  }
}