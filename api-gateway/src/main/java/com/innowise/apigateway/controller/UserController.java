package com.innowise.apigateway.controller;

import com.innowise.apigateway.dto.user.UpdateUserRequest;
import com.innowise.apigateway.dto.user.UserDTO;
import com.innowise.apigateway.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserService userService;


  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<UserDTO>> getUserById(@PathVariable String id) {

    return userService.getUserById(id)
        .map(ResponseEntity::ok)
        .onErrorResume(error -> {
          log.error("User not found with id: {}", id);

          return Mono.just(ResponseEntity.badRequest() // todo вариант с отдельной ошибкой
              .body((new UserDTO(null, null, null, null))));
        });
  }

  @GetMapping("/batch")
  public Mono<ResponseEntity<List<UserDTO>>> getUsersByIds(
      @RequestParam(name = "ids") List<String> ids) {
    return userService.getUserByIds(ids)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.noContent().build())
        .onErrorResume(error -> {
          log.error("Users not found with ids: {}", ids);

          return Mono.just(ResponseEntity.badRequest() // todo вариант с отдельной ошибкой
              .body(List.of(new UserDTO(null, null, null, null))));
        });
  }

  @GetMapping("/email")
  public Mono<ResponseEntity<UserDTO>> getUserByEmail(
      @Valid @RequestParam(name = "email") String email) {
    return userService.getUserByEmail(email)
        .map(ResponseEntity::ok)
        .onErrorResume(error -> {
          log.error("User not found with email: {}", email);

          return Mono.just(ResponseEntity.badRequest() // todo вариант с отдельной ошибкой
              .body(new UserDTO(null, null, null, null)));
        });
  }

  @PatchMapping("/{id}")
  public Mono<ResponseEntity<UserDTO>> updateUser(
      @PathVariable String id,
      @RequestBody @Valid UpdateUserRequest request) {
    return userService.updateUser(id, request)
        .map(ResponseEntity::ok)
        .onErrorResume(error -> {
          log.error("User was not updated with id {}", id);

          return Mono.just(ResponseEntity.badRequest() // todo вариант с отдельной ошибкой
              .body(new UserDTO(null, null, null, null)));
        });
  }

  @DeleteMapping("/{id}")
  public Mono<ResponseEntity<Void>> deleteUser(@PathVariable String id) {
    return userService.deleteUser(id)
        .map(ResponseEntity::ok)
        .onErrorResume(error -> {
          log.error("Delete failed for user {}: {}", id, error.getMessage());
          return Mono.just(ResponseEntity.badRequest().body(null));
        });
  }
}
/**
 * ErrorResponse errorResponse = new ErrorResponse( "DELETE_FAILED", "Failed to delete user: " +
 * error.getMessage(), id ); return Mono.just(ResponseEntity.badRequest().body(errorResponse)); // ←
 * 400 с ошибкой });
 */