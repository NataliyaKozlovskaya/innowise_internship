package com.innowise.authentication.service;

import com.innowise.authentication.dto.LoginRequest;
import com.innowise.authentication.dto.AuthCreateRequest;
import com.innowise.authentication.dto.RefreshTokenRequest;
import com.innowise.authentication.dto.LoginResponse;
import com.innowise.authentication.dto.TokenValidationResponse;
import com.innowise.authentication.entity.UserCredentials;

/**
 * Service interface for managing user credentials and authentication operations
 */
public interface UserCredentialsService {

  /**
   * Authenticates a user with provided credentials and returns JWT tokens
   *
   * @param loginRequest the login credentials containing login and password
   * @return TokenResponse containing access token and refresh token
   */
  LoginResponse login(LoginRequest loginRequest);

  /**
   * Creates new user credentials with the specified login and password
   *
   * @param request credentials for new user
   */
  void createUserCredentials(AuthCreateRequest request);

  /**
   * Refreshes the access token using a valid refresh token. Generates a new access token and
   * optionally rotates the refresh token if it's expiring soon.
   *
   * @param request the refresh token request containing a valid refresh token
   * @return TokenResponse containing new access token and refresh token
   */
  LoginResponse refreshToken(RefreshTokenRequest request);

  /**
   * Validates a JWT token and extracts user information if valid
   *
   * @param token the JWT token to validate
   * @return TokenValidationResponse containing validation status and user information
   */
  TokenValidationResponse validateToken(String token);

  /**
   * Delete user by identifier
   *
   * @param userId user identifier
   */
  void deleteUser(String userId);

  /**
   * Find user by identifier
   *
   * @param userId user identifier
   * @return UserCredentials
   */
  UserCredentials findUserById(String userId);

}
