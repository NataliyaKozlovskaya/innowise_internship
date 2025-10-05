package com.java.project.service;

import com.java.project.dto.security.LoginRequest;
import com.java.project.dto.security.RefreshTokenRequest;
import com.java.project.dto.security.TokenResponse;
import com.java.project.dto.security.TokenValidationResponse;

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
   * Creates new user credentials with the specified login and password. Encodes the password before
   * storing it in the repository.
   *
   * @param login         the username for the new user
   * @param plainPassword the plain text password to be encoded and stored
   */
  void createUserCredentials(String login, String plainPassword);

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
