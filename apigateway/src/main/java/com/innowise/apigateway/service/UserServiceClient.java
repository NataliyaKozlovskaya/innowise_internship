package com.innowise.apigateway.service;

import com.innowise.apigateway.config.ServiceConfig;
import com.innowise.apigateway.dto.auth.recovery.UserCredentialsDataRecovery;
import com.innowise.apigateway.dto.auth.registration.RegistrationRequest;
import com.innowise.apigateway.dto.user.UpdateUserRequest;
import com.innowise.apigateway.dto.user.UserDTO;
import com.innowise.apigateway.dto.user.UserWithCardDTO;
import com.innowise.apigateway.dto.user.registration.UserCreateRequest;
import com.innowise.apigateway.dto.user.registration.UserCreateResponse;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class UserServiceClient {

  private final WebClient webClient;
  private final ServiceConfig serviceConfig;

  public UserServiceClient(WebClient webClient, ServiceConfig serviceConfig) {
    this.webClient = webClient;
    this.serviceConfig = serviceConfig;
  }

  public Mono<UserWithCardDTO> deleteUserInUserService(String id) {
    log.info("API Gateway: Deleting user and retrieving data for id {}", id);

    return webClient.delete()
        .uri(serviceConfig.getUserServiceUrl() + "/api/v1/users/{id}", id)
        .retrieve()
        .bodyToMono(UserWithCardDTO.class)
        .doOnSuccess(userData ->
            log.info("API Gateway: User deleted with data: {}", userData))
        .doOnError(error ->
            log.error("Failed to delete user by id in UserService: {}", error.getMessage()));
  }

  public Mono<UserDTO> updateUserInUserService(String id, UpdateUserRequest request) {
    return webClient.patch()
        .uri(serviceConfig.getUserServiceUrl() + "/api/v1/users/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(UserDTO.class)
        .doOnError(error ->
            log.error("Failed to update user in UserService: {}", error.getMessage()));
  }

  public Mono<UserDTO> getUserByEmailInUserService(String email) {
    return webClient.get()
        .uri(serviceConfig.getUserServiceUrl() + "/api/v1/users/email?email={email}", email)
        .retrieve()
        .bodyToMono(UserDTO.class)
        .doOnError(error ->
            log.error("Failed to get users by email in UserService: {}", error.getMessage()));
  }

  public Mono<UserDTO> getUserByIdInUserService(String id) {
    return webClient.get()
        .uri(serviceConfig.getUserServiceUrl() + "/api/v1/users/{id}", id)
        .retrieve()
        .bodyToMono(UserDTO.class)
        .doOnError(error ->
            log.error("Failed to get user by id in UserService: {}", error.getMessage()));
  }

  public Mono<List<UserDTO>> getUsersByIdsInUserService(List<String> ids) {
    String fullUrl =
        serviceConfig.getUserServiceUrl() + "/api/v1/users/batch?ids=" + String.join(",", ids);

    return webClient.get()
        .uri(fullUrl)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<UserDTO>>() {
        })
        .doOnError(error ->
            log.error("Failed to get users by ids in UserService: {}", error.getMessage()));
  }

  public Mono<Void> deleteUserOrdersInOrderService(String id) {
    return webClient.delete()
        .uri(serviceConfig.getOrderServiceUrl() + "/api/v1/orders/user/{id}", id)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnError(
            error -> log.error("API Gateway: Failed to delete orders for user {}", id, error));
  }

  public Mono<UserCredentialsDataRecovery> deleteUserInAuthService(String id) {
    log.info("API Gateway: Preparing to delete user from AuthService: {}", id);

    return webClient.delete()
        .uri(serviceConfig.getAuthServiceUrl() + "/api/v1/auth/internal/{id}", id)
        .retrieve()
        .onStatus(HttpStatusCode::isError, response ->
            Mono.error(new RuntimeException("Auth Service error: " + response.statusCode())))
        .bodyToMono(UserCredentialsDataRecovery.class)
        .doOnError(error ->
            log.error("Failed to delete card by id in CardService: {}", error.getMessage()));
  }

  public Mono<UserCreateResponse> createUserInUserService(RegistrationRequest request) {
    String uuid = UUID.randomUUID().toString();
    log.info("Generated UUID for user: {}", uuid);

    UserCreateRequest userRequest = new UserCreateRequest(
        uuid,
        request.name(),
        request.surname(),
        request.birthDate(),
        request.email());

    return webClient.post()
        .uri(serviceConfig.getUserServiceUrl() + "/api/v1/users/register")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(userRequest)
        .retrieve()
        .bodyToMono(UserCreateResponse.class)
        .doOnError(
            error -> log.error("Failed to create user in User Service: {}", error.getMessage()));
  }
}
