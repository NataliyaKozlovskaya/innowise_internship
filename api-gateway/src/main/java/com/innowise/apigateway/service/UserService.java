package com.innowise.apigateway.service;

import com.innowise.apigateway.config.ServiceConfig;
import com.innowise.apigateway.dto.card.CardDTO;
import com.innowise.apigateway.dto.card.CreateCardRequest;
import com.innowise.apigateway.dto.user.UpdateUserRequest;
import com.innowise.apigateway.dto.user.UserDTO;
import com.innowise.apigateway.dto.user.UserWithCardDTO;
import com.innowise.apigateway.dto.user.registration.UserCreateRequest;
import com.innowise.apigateway.dto.user.registration.UserResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class UserService {

  private final WebClient webClient;
  private final ServiceConfig serviceConfig;


  public UserService(WebClient webClient, ServiceConfig serviceConfig) {
    this.webClient = webClient;
    this.serviceConfig = serviceConfig;
  }

  public Mono<UserDTO> getUserById(String id) {
    log.info("API Gateway: Starting find user in UserService: {}", id);

    return getUserByIdInUserService(id)
        .flatMap(userDTO -> {
          log.info("API Gateway: get user by id {} successful", id);
          return Mono.just(new UserDTO(userDTO.name(), userDTO.surname(), userDTO.birthDate(),
              userDTO.email()));
        })
        .onErrorResume(error -> {
          log.error("API Gateway: Get user by id failed", error.getMessage());
          return Mono.error(new RuntimeException("Get user by id failed", error));
        });
  }

  public Mono<List<UserDTO>> getUserByIds(List<String> ids) {
    log.info("API Gateway: Starting find users in UserService: {}", ids);

    return getUsersByIdsInUserService(ids)
        .flatMap(userDTO -> {
          log.info("API Gateway: get user by id {} successful", ids);
          return Mono.just(
              List.of(new UserDTO(userDTO.name(), userDTO.surname(), userDTO.birthDate(),
                  userDTO.email())));
        })
        .onErrorResume(error -> {
          log.error("API Gateway: Get users by ids failed", error.getMessage());
          return Mono.error(new RuntimeException("Get users by ids failed", error));
        });
  }

  public Mono<UserDTO> getUserByEmail(String email) {
    log.info("API Gateway: Starting find user by email in UserService: {}", email);
    return getUserByEmailInUserService(email)
        .flatMap(userDTO -> {
          log.info("API Gateway: get user by email {} successful", email);
          return Mono.just(new UserDTO(userDTO.name(), userDTO.surname(), userDTO.birthDate(),
              userDTO.email()));
        })
        .onErrorResume(error -> {
          log.error("API Gateway: Get user by email failed", error.getMessage());
          return Mono.error(new RuntimeException("Get user by email failed", error));
        });
  }

  public Mono<UserDTO> updateUser(String id, UpdateUserRequest request) {
    log.info("API Gateway: Starting update user with id {} in UserService: {}", id);
    return updateUserInUserService(id, request)
        .flatMap(userDTO -> {
          log.info("API Gateway: update user by id {} successful", id);
          return Mono.just(new UserDTO(userDTO.name(), userDTO.surname(), userDTO.birthDate(),
              userDTO.email()));
        })
        .onErrorResume(error -> {
          log.error("API Gateway: Update user by id failed", error.getMessage());
          return Mono.error(new RuntimeException("Update user by id failed", error));
        });
  }

  /**
   * Deleting a user and his cards from the User Service and the Auth Service
   */
  public Mono<Void> deleteUser(String id) {
    log.info("API Gateway: Starting delete user with id {}", id);
    return deleteUserInUserService(id)
        .flatMap(userResponse -> {
          log.info("API Gateway: Starting to delete user from UserService");

          return deleteUserInAuthService(id)
              .then()
              .onErrorResume(error ->
                  rollbackUserDeletion(id, userResponse)
                      .then(rollbackCardDeletion(id, userResponse))
                      .then(Mono.error(
                          new RuntimeException("Deletion user failed in auth service", error)))
              );
        })
        .onErrorResume(error -> {
          log.error("API Gateway: Delete user failed for id {}: {}", id, error.getMessage());
          return Mono.error(new RuntimeException("Delete user failed", error));
        });
  }

  private Mono<UserResponse> rollbackUserDeletion(String id, UserWithCardDTO userResponse) {
    UserCreateRequest user = new UserCreateRequest(
        id,
        userResponse.name(),
        userResponse.surname(),
        userResponse.birthDate(),
        userResponse.email());

    return webClient.post()
        .uri(serviceConfig.getUserServiceUrl() + "/api/v1/users")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(user)
        .retrieve()
        .bodyToMono(UserResponse.class)
        .doOnError(
            error -> log.error("Failed to create user in User Service: {}", error.getMessage()));
  }

  /**
   * Rollback of deleted user cards
   */
  private Mono<List<CardDTO>> rollbackCardDeletion(String id, UserWithCardDTO userResponse) {
    if (userResponse.cardDTOList().isEmpty()) {
      return Mono.just(List.of());
    }

    log.info("Restoring {} cards for user: {}", userResponse.cardDTOList().size(), id);

    return Flux.fromIterable(userResponse.cardDTOList())
        .flatMap(cardDTO -> {
          CreateCardRequest cardRequest = new CreateCardRequest(
              cardDTO.number(),
              cardDTO.holder(),
              cardDTO.expirationDate()
          );

          return webClient.post()
              .uri(uriBuilder -> uriBuilder
                  .path(serviceConfig.getCardServiceUrl() + "/api/v1/cards")
                  .queryParam("userId", id)
                  .build())
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(cardRequest)
              .retrieve()
              .bodyToMono(CardDTO.class)
              .onErrorResume(error -> {
                log.warn("Failed to restore card {} for user {}, skipping: {}",
                    cardDTO.number(), id, error.getMessage());
                return Mono.empty(); // Skip failed cards
              });
        })
        .collectList()
        .doOnSuccess(cards -> {
          if (cards.size() < userResponse.cardDTOList().size()) {
            log.warn("Partially restored cards: {}/{} for user: {}",
                cards.size(), userResponse.cardDTOList().size(), id);
          } else {
            log.info("All {} cards restored for user: {}", cards.size(), id);
          }
        });
  }

  private Mono<UserWithCardDTO> deleteUserInUserService(String id) {
    return webClient.delete()
        .uri(serviceConfig.getUserServiceUrl() + "/api/v1/users/{id}", id)
        .retrieve()
        .bodyToMono(UserWithCardDTO.class)
        .doOnError(
            error -> log.error("Failed to delete user by id in User Service: {}",
                error.getMessage()));
  }

  private Mono<Void> deleteUserInAuthService(String id) {
    return webClient.delete()
        .uri(serviceConfig.getAuthServiceUrl() + "/api/v1/auth/{id}", id)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnError(
            error -> log.error("Failed to delete user by id in Auth Service: {}",
                error.getMessage()));
  }

  private Mono<UserDTO> updateUserInUserService(String id, UpdateUserRequest request) {
    return webClient.patch()
        .uri(serviceConfig.getUserServiceUrl() + "/api/v1/users/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(UserDTO.class)
        .doOnError(
            error -> log.error("Failed to get users by email in User Service: {}",
                error.getMessage()));
  }

  private Mono<UserDTO> getUserByEmailInUserService(String email) {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path(serviceConfig.getUserServiceUrl() + "/api/v1/users/email")
            .queryParam("email", email)
            .build())

        .retrieve()
        .bodyToMono(UserDTO.class)
        .doOnError(
            error -> log.error("Failed to get users by email in User Service: {}",
                error.getMessage()));
  }

  private Mono<UserDTO> getUserByIdInUserService(String id) {
    return webClient.get()
        .uri(serviceConfig.getUserServiceUrl() + "/api/v1/users/{id}", id)

        .retrieve()
        .bodyToMono(UserDTO.class)
        .doOnError(
            error -> log.error("Failed to get user by id in User Service: {}", error.getMessage()));
  }

  private Mono<UserDTO> getUsersByIdsInUserService(List<String> ids) {
    return webClient.get()

        .uri(uriBuilder -> uriBuilder
            .path(serviceConfig.getUserServiceUrl() + "/api/v1/users")
            .queryParam("ids", String.join(",", ids))
            .build())

        .retrieve()
        .bodyToMono(UserDTO.class)
        .doOnError(
            error -> log.error("Failed to get users by ids in User Service: {}",
                error.getMessage()));
  }
}
