package com.innowise.apigateway.manager;

import com.innowise.apigateway.config.ServiceConfig;
import com.innowise.apigateway.dto.auth.recovery.UserCredentialsDataRecovery;
import com.innowise.apigateway.dto.card.CardDTO;
import com.innowise.apigateway.dto.card.CreateCardRequest;
import com.innowise.apigateway.dto.user.UserWithCardDTO;
import com.innowise.apigateway.dto.user.registration.UserCreateRequest;
import com.innowise.apigateway.dto.user.registration.UserCreateResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class RollbackManager {

  private final WebClient webClient;
  private final ServiceConfig serviceConfig;

  public RollbackManager(WebClient webClient, ServiceConfig serviceConfig) {
    this.webClient = webClient;
    this.serviceConfig = serviceConfig;
  }

  /**
   * Rollback of user creation
   */
  public Mono<Void> rollbackUser(String userId, String login) {
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
          return Mono.empty();
        });
  }

  private Mono<Void> rollbackUserCreation(String id) {
    log.info("Rolling back user creation for: {}", id);

    return webClient.delete()
        .uri("http://user-service:8088/api/v1/users/internal/{id}", id)
        .retrieve()
        .toBodilessEntity()
        .then()
        .onErrorResume(error -> {
          log.warn("User rollback failed for {}: {}. User might not exist.", id,
              error.getMessage());
          return Mono.empty();
        });
  }

  /**
   * Rollback of deleted user credentials
   */
  public Mono<Void> rollbackAuthServiceDeletion(UserCredentialsDataRecovery data) {
    log.info("API Gateway: Rolling back AuthService deletion for user {}", data.uuid());
    return webClient.post()
        .uri(serviceConfig.getAuthServiceUrl() + "/api/v1/auth/recovery/register")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(data)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnError(error ->
            log.error(
                "Recovering for userId {} failed in AuthService. Manual intervention is required: {}",
                data.uuid(), error.getMessage()));
  }

  /**
   * Rollback of deleted user
   */
  public Mono<UserCreateResponse> rollbackUserDeletion(String userId,
      UserWithCardDTO userResponse) {
    log.info("API Gateway: Rolling back UserService deletion for user {}", userId);
    UserCreateRequest user = new UserCreateRequest(
        userId,
        userResponse.name(),
        userResponse.surname(),
        userResponse.birthDate(),
        userResponse.email());

    return webClient.post()
        .uri(serviceConfig.getUserServiceUrl() + "/api/v1/users")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(user)
        .retrieve()
        .bodyToMono(UserCreateResponse.class)
        .doOnError(error ->
            log.error("Failed to create user in UserService: {}", error.getMessage()));
  }

  /**
   * Rollback of deleted user cards
   */
  public Mono<List<CardDTO>> rollbackCardDeletion(String id, UserWithCardDTO userResponse) {
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
              .uri(serviceConfig.getCardServiceUrl() + "/api/v1/cards?userId={userId}", id)
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(cardRequest)
              .retrieve()
              .bodyToMono(CardDTO.class)
              .onErrorResume(error -> {
                log.warn("Failed to restore card {} for user {}, skipping: {}",
                    cardDTO.number(), id, error.getMessage());
                return Mono.empty();
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
}
