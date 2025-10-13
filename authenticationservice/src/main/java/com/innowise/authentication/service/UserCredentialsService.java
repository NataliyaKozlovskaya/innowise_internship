package com.innowise.authentication.service;

import com.innowise.authentication.dto.LoginRequest;
import com.innowise.authentication.dto.RegisterRequest;
import com.innowise.authentication.dto.RefreshTokenRequest;
import com.innowise.authentication.dto.TokenResponse;
import com.innowise.authentication.dto.TokenValidationResponse;

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
  TokenResponse login(LoginRequest loginRequest);

  /**
   * Creates new user credentials with the specified login and password
   *
   * @param request credentials for new user
   */
  void createUserCredentials(RegisterRequest request);

  /**
   * Refreshes the access token using a valid refresh token. Generates a new access token and
   * optionally rotates the refresh token if it's expiring soon.
   *
   * @param request the refresh token request containing a valid refresh token
   * @return TokenResponse containing new access token and refresh token
   */
  TokenResponse refreshToken(RefreshTokenRequest request);

  /**
   * Validates a JWT token and extracts user information if valid
   *
   * @param token the JWT token to validate
   * @return TokenValidationResponse containing validation status and user information
   */
  TokenValidationResponse validateToken(String token);
}
