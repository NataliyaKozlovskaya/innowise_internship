package com.java.project.controller;

import com.java.project.dto.user.CreateUserRequest;
import com.java.project.dto.user.UpdateUserRequest;
import com.java.project.dto.user.UserDTO;
import com.java.project.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
 * card management.
 */
@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

  private final UserService userService;

  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
  }

  /**
   * Create a new user
   */
  @PostMapping
  public ResponseEntity<UserDTO> createUser(@RequestBody @Valid CreateUserRequest request) {
    UserDTO createdUser = userService.createUser(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
  }

  /**
   * Get user by ID
   */
  @GetMapping("/{id}")
  public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
    UserDTO response = userService.getUserById(id);
    return ResponseEntity.ok(response);
  }

  /**
   * Get users by list of IDs
   */
  @GetMapping("/batch")
  public ResponseEntity<List<UserDTO>> getUsersByIds(@RequestParam(name = "ids") List<Long> ids) {
    List<UserDTO> responses = userService.getUsersByIds(ids);
    return ResponseEntity.ok(responses);
  }

  /**
   * Get user by email address
   */
  @GetMapping("/email")
  public ResponseEntity<UserDTO> getUserByEmail(@Valid @RequestParam(name = "email") String email) {
    UserDTO response = userService.getUserByEmail(email);
    return ResponseEntity.ok(response);
  }

  /**
   * Update user information
   */
  @PatchMapping("/{id}")
  public ResponseEntity<UserDTO> updateUser(
      @PathVariable Long id,
      @RequestBody @Valid UpdateUserRequest request) {
    UserDTO updatedUser = userService.updateUser(id, request);
    return ResponseEntity.ok(updatedUser);
  }

  /**
   * Delete user by ID
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }
}
