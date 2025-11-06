package com.innowise.apigateway.manager;

import com.innowise.apigateway.dto.user.UpdateUserRequest;
import com.innowise.apigateway.dto.user.UserDTO;
import com.innowise.apigateway.service.UserServiceClient;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class UserOperationManager {

  private final UserServiceClient userClient;
  private final RollbackManager rollbackManager;

  public UserOperationManager(UserServiceClient userClient, RollbackManager rollbackManager) {
    this.userClient = userClient;
    this.rollbackManager = rollbackManager;
  }

  /**
   * Getting a user by id
   */
  public Mono<UserDTO> getUserById(String id) {
    log.info("API Gateway: Starting find user in UserService: {}", id);

    return userClient.getUserByIdInUserService(id)
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

  /**
   * Getting a list of users by IDs
   */
  public Mono<List<UserDTO>> getUserByIds(List<String> ids) {
    log.info("API Gateway: Starting find users in UserService: {}", ids);
    return userClient.getUsersByIdsInUserService(ids)
        .doOnSuccess(userDTOs ->
            log.info("API Gateway: get users by ids {} successful. Found {} users", ids,
                userDTOs.size()))
        .map(userDTOs -> userDTOs.stream()
            .map(user -> new UserDTO(user.name(), user.surname(), user.birthDate(), user.email()))
            .toList()
        )
        .onErrorResume(error -> {
          log.error("API Gateway: Get users by ids failed", error.getMessage());
          return Mono.error(new RuntimeException("Get users by ids failed", error));
        });
  }

  /**
   * Getting a user by email
   */
  public Mono<UserDTO> getUserByEmail(String email) {
    log.info("API Gateway: Starting find user by email in UserService: {}", email);
    return userClient.getUserByEmailInUserService(email)
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

  /**
   * User data update
   */
  public Mono<UserDTO> updateUser(String id, UpdateUserRequest request) {
    log.info("API Gateway: Starting update user with id {} in UserService: {}", id);
    return userClient.updateUserInUserService(id, request)
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
   * Deleting a user and his cards
   */
  public Mono<Void> deleteUser(String id) {
    log.info("API Gateway: Starting delete user with id {}", id);

    return userClient.deleteUserInUserService(id)
        .doOnSubscribe(s -> log.info("API Gateway: UserService deletion subscribed"))
        .doOnSuccess(userData ->
            log.info("API Gateway: UserService deletion completed. User data received"))
        .flatMap(userData -> {
          log.info("API Gateway: Starting AuthService deletion for id {}", id);

          return userClient.deleteUserInAuthService(id)
              .doOnSubscribe(s -> log.info("API Gateway: AuthService deletion subscribed"))
              .doOnSuccess(authResult ->
                  log.info("API Gateway: AuthService deletion successful for id {}", id))
              .flatMap(authResult -> {
                log.info("API Gateway: Starting OrderService deletion for id {}", id);

                return userClient.deleteUserOrdersInOrderService(id)
                    .doOnSubscribe(s -> log.info("API Gateway: OrderService deletion subscribed"))
                    .doOnSuccess(orderResult ->
                        log.info("API Gateway: OrderService deletion successful for id {}", id))
                    .onErrorResume(error -> {
                      log.error(
                          "API Gateway: OrderService deletion failed, initiating rollback for id {}",
                          id);
                      return rollbackManager.rollbackAuthServiceDeletion(authResult)
                          .then(rollbackManager.rollbackUserDeletion(id, userData))
                          .then(rollbackManager.rollbackCardDeletion(id, userData))
                          .then(Mono.error(
                              new RuntimeException("Deletion failed in order service", error)));
                    });
              })

              .onErrorResume(error -> {
                log.error(
                    "API Gateway: AuthService deletion failed, initiating rollback for id {}",
                    id);
                return rollbackManager.rollbackUserDeletion(id, userData)
                    .then(rollbackManager.rollbackCardDeletion(id, userData))
                    .then(
                        Mono.error(new RuntimeException("Deletion failed in auth service", error)));
              });
        })
        .doOnSuccess(v -> log.info("API Gateway: User {} fully deleted from all services", id))
        .onErrorResume(error -> {
          log.error("API Gateway: Overall deletion failed: {}", error.getMessage());
          return Mono.error(new RuntimeException("Delete user failed", error));
        });
  }
}
