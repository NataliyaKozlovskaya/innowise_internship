package com.innowise.authentication.controller;

import com.innowise.authentication.dto.LoginRequest;
import com.innowise.authentication.dto.AuthCreateRequest;
import com.innowise.authentication.dto.RefreshTokenRequest;
import com.innowise.authentication.dto.LoginResponse;
import com.innowise.authentication.dto.TokenValidationResponse;
import com.innowise.authentication.service.UserCredentialsService;
import com.innowise.authentication.service.impl.UserCredentialsServiceImpl;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * REST controller for handling user authentication and authorization operations
 */
@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthenticationController {

  private final UserCredentialsService userCredentialsService;


  public AuthenticationController(UserCredentialsServiceImpl userCredentialsService) {
    this.userCredentialsService = userCredentialsService;
  }

  /**
   * Authenticates a user and returns JWT tokens upon successful login
   */
  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
    LoginResponse response = userCredentialsService.login(loginRequest);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  /**
   * Refreshes the access token using a valid refresh token. Generates new access token and
   * optionally rotates refresh token if expiring soon.
   */
  @PostMapping("/refresh")
  public ResponseEntity<LoginResponse> refreshToken(
      @Valid @RequestBody RefreshTokenRequest request) {
    LoginResponse response = userCredentialsService.refreshToken(request);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  /**
   * Validates a JWT token and returns its status and claims if valid
   */
  @PostMapping("/validate")
  public ResponseEntity<TokenValidationResponse> validateToken(@RequestParam String token) {
    TokenValidationResponse response = userCredentialsService.validateToken(token);
    return ResponseEntity.ok(response);
  }

  /**
   * Registers a new user with the provided credentials
   */
  @PostMapping("/register")
  public ResponseEntity<Void> register(@Valid @RequestBody AuthCreateRequest request) {
    userCredentialsService.createUserCredentials(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  /**
   * Delete user by ID
   */
  @DeleteMapping("/internal/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    userCredentialsService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }
}
