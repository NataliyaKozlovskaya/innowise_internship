package com.innowise.authentication.service.impl;

import com.innowise.authentication.dto.RegisterRequest;
import com.innowise.authentication.dto.RefreshTokenRequest;
import com.innowise.authentication.dto.TokenResponse;
import com.innowise.authentication.dto.TokenValidationResponse;
import com.innowise.authentication.dto.CreateUserRequest;
import com.innowise.authentication.entity.UserCredentials;
import com.innowise.authentication.entity.UserRole;
import com.innowise.authentication.exception.InvalidTokenException;
import com.innowise.authentication.exception.UserAlreadyExistsException;
import com.innowise.authentication.exception.UserCreationException;
import com.innowise.authentication.feign.UserServiceClient;
import com.innowise.authentication.service.UserCredentialsService;

import com.innowise.authentication.repository.UserCredentialsRepository;
import com.innowise.authentication.repository.UserRoleRepository;
import com.innowise.authentication.security.JwtTokenProvider;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class UserCredentialsServiceImpl implements UserCredentialsService {

  private static final String ROLE = "ROLE_USER";
  private final UserCredentialsRepository userCredentialsRepository;
  private final UserRoleRepository userRoleRepository;
  private  final UserServiceClient userServiceClient;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;


  public UserCredentialsServiceImpl(UserCredentialsRepository userCredentialsRepository,
      UserRoleRepository userRoleRepository, UserServiceClient userServiceClient, PasswordEncoder passwordEncoder,
      JwtTokenProvider jwtTokenProvider
  ) {
    this.userCredentialsRepository = userCredentialsRepository;
    this.userRoleRepository = userRoleRepository;
    this.userServiceClient = userServiceClient;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  public TokenResponse login(RegisterRequest loginRequest) {
    log.info("Login attempt for user: {}", loginRequest.login());

    if (validateCredentials(loginRequest.login(), loginRequest.password())) {
      UserCredentials user = userCredentialsRepository.findById(loginRequest.login())
          .orElseThrow(() -> new BadCredentialsException("User not found"));

      List<String> authorities = user.getRolesAsStrings();

      String accessToken = jwtTokenProvider.generateAccessToken(loginRequest.login(), authorities);
      String refreshToken = jwtTokenProvider.generateRefreshToken(loginRequest.login());

      return new TokenResponse(accessToken, refreshToken);
    } else {
      throw new BadCredentialsException("Invalid login or password");
    }
  }

  @Override
  public void createUserCredentials(RegisterRequest request) {
    log.info("User registration: {}", request.login());

    String userId = UUID.randomUUID().toString();

    if (userCredentialsRepository.existsById(request.login())) {
      throw new UserAlreadyExistsException(
          "User with login " + request.login() + " already exists");
    }

    CreateUserRequest createUserRequest = new CreateUserRequest(
        userId, request.name(), request.surname(), request.birthDate(), request.email());

    try {
      userServiceClient.createUser(createUserRequest);
    } catch (Exception e) {
      log.error("Failed to create user profile: " + e.getMessage());
      throw new UserCreationException("Failed to create user profile: " + e.getMessage());
    }

    UserCredentials credentials = new UserCredentials();
    credentials.setLogin(request.login());
    credentials.setPasswordHash(passwordEncoder.encode(request.password()));

    UserRole userRole = new UserRole();
    userRole.setUserLogin(request.login());
    userRole.setRole(ROLE);

    userCredentialsRepository.save(credentials);
    userRoleRepository.save(userRole);
    log.info("User credentials created for login: {}", request.login());
  }

  @Override
  public TokenValidationResponse validateToken(String token) {
    log.info("Token validation request");

    boolean isValid = jwtTokenProvider.validateToken(token);

    TokenValidationResponse response = new TokenValidationResponse(isValid, null, null);

    if (isValid) {
      String username = jwtTokenProvider.getUsernameFromToken(token);
      List<String> authorities = jwtTokenProvider.getAuthoritiesFromToken(token);
      response = response.withUsername(username).withAuthorities(authorities);
    }
    return response;
  }

  @Override
  public TokenResponse refreshToken(RefreshTokenRequest request) {
    log.info("Refresh token request");

    if (jwtTokenProvider.validateToken(request.refreshToken())) {
      String username = jwtTokenProvider.getUsernameFromToken(request.refreshToken());

      List<String> authorities = List.of(ROLE);
      String newAccessToken = jwtTokenProvider.generateAccessToken(username, authorities);

      String newRefreshToken = jwtTokenProvider.isRefreshTokenExpiringSoon(request.refreshToken())
          ? jwtTokenProvider.generateRefreshToken(username)
          : request.refreshToken();

      return new TokenResponse(newAccessToken, newRefreshToken);
    } else {
      throw new InvalidTokenException("Invalid refresh token");
    }
  }

  private boolean validateCredentials(String login, String plainPassword) {
    return userCredentialsRepository.findById(login)
        .map(credentials -> passwordEncoder.matches(plainPassword, credentials.getPasswordHash()))
        .orElse(false);
  }
}