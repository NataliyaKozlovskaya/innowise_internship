package com.innowise.user.controller;

import com.innowise.user.dto.user.CreateUserRequest;
import com.innowise.user.dto.user.UpdateUserRequest;
import com.innowise.user.dto.user.UserCreateResponse;
import com.innowise.user.dto.user.UserDTO;
import com.innowise.user.dto.user.UserWithCardDTO;
import com.innowise.user.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for managing user operations. Provides endpoints for user CRUD operations and
 * card management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@Validated
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  /**
   * Create a new user
   */
  @PostMapping("/register")
  public ResponseEntity<UserCreateResponse> createUser(
      @RequestBody @Valid CreateUserRequest request) {
    UserCreateResponse createdUser = userService.createUser(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
  }

  /**
   * Get user by ID
   */
  @GetMapping("/{id}")
  public ResponseEntity<UserCreateResponse> getUserById(@PathVariable String id) {
    UserCreateResponse response = userService.getUserById(id);
    return ResponseEntity.ok(response);
  }

  /**
   * Get users by list of IDs
   */
  @GetMapping("/batch")
  public ResponseEntity<List<UserDTO>> getUsersByIds(@RequestParam(name = "ids") List<String> ids) {
    List<UserDTO> responses = userService.getUsersByIds(ids);
    return ResponseEntity.ok(responses);
  }

  /**
   * Get user by email address
   */
  @GetMapping("/email")
  public ResponseEntity<UserCreateResponse> getUserByEmail(
      @Valid @RequestParam(name = "email") String email) {
    UserCreateResponse response = userService.getUserByEmail(email);
    return ResponseEntity.ok(response);
  }

  /**
   * Update user information
   */
  @PatchMapping("/{id}")
  public ResponseEntity<UserDTO> updateUser(
      @PathVariable String id,
      @RequestBody @Valid UpdateUserRequest request) {
    UserDTO updatedUser = userService.updateUser(id, request);
    return ResponseEntity.ok(updatedUser);
  }

  /**
   * Delete user by ID
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<UserWithCardDTO> deleteUser(@PathVariable String id) {
    UserWithCardDTO response = userService.deleteUser(id);
    return ResponseEntity.ok(response);
  }

  /**
   * Internal delete user by ID
   */
  @DeleteMapping("/internal/{id}")
  public ResponseEntity<Void> internalDeleteUser(@PathVariable String id) {
    userService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }
}
