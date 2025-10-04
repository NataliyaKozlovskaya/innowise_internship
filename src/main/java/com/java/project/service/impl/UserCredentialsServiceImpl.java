package com.java.project.service.impl;

import com.java.project.dto.security.LoginRequest;
import com.java.project.dto.security.RefreshTokenRequest;
import com.java.project.dto.security.TokenResponse;
import com.java.project.dto.security.TokenValidationResponse;
import com.java.project.entity.UserCredentials;
import com.java.project.entity.UserRole;
import com.java.project.exception.InvalidTokenException;
import com.java.project.exception.UserAlreadyExistsException;
import com.java.project.repository.UserCredentialsRepository;
import com.java.project.repository.UserRoleRepository;
import com.java.project.security.JwtTokenProvider;
import com.java.project.service.UserCredentialsService;
import java.util.List;
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
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;


  public UserCredentialsServiceImpl(UserCredentialsRepository userCredentialsRepository,
      UserRoleRepository userRoleRepository, PasswordEncoder passwordEncoder,
      JwtTokenProvider jwtTokenProvider
  ) {
    this.userCredentialsRepository = userCredentialsRepository;
    this.userRoleRepository = userRoleRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  public TokenResponse login(LoginRequest loginRequest) {
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
  public void createUserCredentials(String login, String plainPassword) {
    log.info("User registration: {}", login);
    if (userCredentialsRepository.existsById(login)) {
      throw new UserAlreadyExistsException("User with login " + login + " already exists");
    }

    UserCredentials credentials = new UserCredentials();
    credentials.setLogin(login);
    credentials.setPasswordHash(passwordEncoder.encode(plainPassword));

    UserRole userRole = new UserRole();
    userRole.setUserLogin(login);
    userRole.setRole(ROLE);

    userCredentialsRepository.save(credentials);
    userRoleRepository.save(userRole);
    log.info("User credentials created for login: {}", login);
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