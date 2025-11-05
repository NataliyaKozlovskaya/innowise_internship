package com.innowise.apigateway.handler;

import com.innowise.apigateway.dto.user.UpdateUserRequest;
import com.innowise.apigateway.manager.UserOperationManager;
import java.util.Arrays;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Spring WebFlux handler handling user-related HTTP endpoints
 */
@Slf4j
@Component
public class UserHandler {

  private final UserOperationManager userOperationManager;

  public UserHandler(UserOperationManager userOperationManager) {
    this.userOperationManager = userOperationManager;
  }

  public Mono<ServerResponse> getUserById(ServerRequest request) {
    String id = request.pathVariable("id");
    log.info("Getting user by id: {}", id);

    return userOperationManager.getUserById(id)
        .flatMap(user -> ServerResponse.ok().bodyValue(user))
        .switchIfEmpty(ServerResponse.notFound().build())
        .onErrorResume(error -> {
          log.error("Error getting user with id {}: {}", id, error.getMessage());
          return ServerResponse.badRequest().bodyValue("Error retrieving user");
        });
  }

  public Mono<ServerResponse> getUsersByIds(ServerRequest request) {
    return Mono.justOrEmpty(request.queryParam("ids"))
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Ids parameter is required")))
        .map(ids -> Arrays.asList(ids.split(",")))
        .flatMap(userOperationManager::getUserByIds)
        .flatMap(users -> ServerResponse.ok().bodyValue(users))
        .onErrorResume(error -> {
          if (error instanceof IllegalArgumentException) {
            log.warn("Ids parameter is missing");
            return ServerResponse.badRequest().bodyValue("Ids parameter is required");
          }
          log.error("Error getting users by ids", error);
          return ServerResponse.ok().bodyValue(Collections.emptyList());
        });
  }

  public Mono<ServerResponse> getUserByEmail(ServerRequest request) {
    return Mono.justOrEmpty(request.queryParam("email"))
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Email parameter is required")))
        .flatMap(userOperationManager::getUserByEmail)
        .flatMap(user -> ServerResponse.ok().bodyValue(user))
        .onErrorResume(error -> {
          if (error instanceof IllegalArgumentException) {
            log.warn("Email parameter is missing");
            return ServerResponse.badRequest().bodyValue("Email parameter is required");
          }
          log.error("User not found with email: {}", request.queryParam("email").orElse(""), error);
          return ServerResponse.notFound().build();
        });
  }

  public Mono<ServerResponse> updateUser(ServerRequest request) {
    String id = request.pathVariable("id");

    return request.bodyToMono(UpdateUserRequest.class)
        .flatMap(updateRequest -> userOperationManager.updateUser(id, updateRequest))
        .flatMap(user -> ServerResponse.ok().bodyValue(user))
        .onErrorResume(error -> {
          log.error("User was not updated with id {}", id, error);
          return ServerResponse.notFound().build();
        });
  }

  public Mono<ServerResponse> deleteUser(ServerRequest request) {
    String id = request.pathVariable("id");

    return userOperationManager.deleteUser(id)
        .then(ServerResponse.ok().build())
        .onErrorResume(error -> {
          log.error("Delete failed for user {}: {}", id, error.getMessage());
          return ServerResponse.badRequest().build();
        });
  }
}
