package com.innowise.authentication.service.impl;

import com.innowise.authentication.dto.LoginRequest;
import com.innowise.authentication.dto.RefreshTokenRequest;
import com.innowise.authentication.dto.AuthCreateRequest;
import com.innowise.authentication.dto.LoginResponse;
import com.innowise.authentication.dto.TokenValidationResponse;
import com.innowise.authentication.dto.UserCredentialsDataRecovery;
import com.innowise.authentication.dto.UserRoleDTO;
import com.innowise.authentication.entity.UserCredentials;
import com.innowise.authentication.entity.UserRole;
import com.innowise.authentication.exception.InvalidTokenException;
import com.innowise.authentication.exception.UserAlreadyExistsException;
import com.innowise.authentication.exception.UserNotFoundException;
import com.innowise.authentication.mapper.UserRoleMapper;
import com.innowise.authentication.repository.UserCredentialsRepository;
import com.innowise.authentication.repository.UserRoleRepository;
import com.innowise.authentication.rest.UserServiceClient;
import com.innowise.authentication.security.JwtTokenProvider;
import com.innowise.authentication.service.UserCredentialsService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class UserCredentialsServiceImpl implements UserCredentialsService {

  private static final String ROLE = "ROLE_USER";
  private static final String USER_NOT_FOUND = "User not found with id: ";
  private final UserCredentialsRepository userCredentialsRepository;
  private final UserRoleRepository userRoleRepository;
  private final UserServiceClient userServiceClient;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserRoleMapper userRoleMapper;

  public UserCredentialsServiceImpl(UserCredentialsRepository userCredentialsRepository,
      UserRoleRepository userRoleRepository, UserServiceClient userServiceClient,
      PasswordEncoder passwordEncoder,
      JwtTokenProvider jwtTokenProvider,
      UserRoleMapper userRoleMapper) {
    this.userCredentialsRepository = userCredentialsRepository;
    this.userRoleRepository = userRoleRepository;
    this.userServiceClient = userServiceClient;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenProvider = jwtTokenProvider;
    this.userRoleMapper = userRoleMapper;
  }

  @Override
  public LoginResponse login(LoginRequest loginRequest) {
    log.info("Login attempt for user: {}", loginRequest.login());

    UserCredentials user = userCredentialsRepository.findByLoginWithRoles(loginRequest.login())
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (!passwordEncoder.matches(loginRequest.password(), user.getPasswordHash())) {
      throw new RuntimeException("Invalid password");
    }

    List<String> authorities = user.getRolesAsStrings();
    String accessToken = jwtTokenProvider.generateAccessToken(loginRequest.login(), authorities);
    String refreshToken = jwtTokenProvider.generateRefreshToken(loginRequest.login());

    return new LoginResponse(accessToken, refreshToken);
  }

  @Override
  public void createUserCredentials(AuthCreateRequest request) {
    log.info("User registration: {}", request.login());

    if (userCredentialsRepository.existsById(request.login())) {
      throw new UserAlreadyExistsException(
          "User with login " + request.login() + " already exists");
    }

    UserCredentials credentials = new UserCredentials();
    credentials.setUuid(request.uuid());
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
  public void recoveryUserCredentials(UserCredentialsDataRecovery request) {
    log.info("User recovery credentials with id: {}, login: {}", request.uuid(), request.login());

    if (userCredentialsRepository.existsById(request.login())) {
      throw new UserAlreadyExistsException(
          "User with login " + request.login()
              + " already exists. Recovering failed. Manual intervention is required");
    }

    UserCredentials credentials = new UserCredentials();
    credentials.setUuid(request.uuid());
    credentials.setLogin(request.login());
    credentials.setPasswordHash(request.passwordHash());
    credentials.setCreatedAt(request.createdAt());
    credentials.setUpdatedAt(request.updatedAt());

    List<UserRole> userRoles = request.roles().stream()
        .map(roleDto -> {
          UserRole userRole = new UserRole();
          userRole.setUserLogin(request.login());
          userRole.setRole(roleDto.role());
          return userRole;
        })
        .collect(Collectors.toList());

    userCredentialsRepository.save(credentials);
    userRoleRepository.saveAll(userRoles);
    log.info("Recovering user`s credentials was successful, for login: {}", request.login());
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
  public LoginResponse refreshToken(RefreshTokenRequest request) {
    log.info("Refresh token request");

    if (jwtTokenProvider.validateToken(request.refreshToken())) {
      String username = jwtTokenProvider.getUsernameFromToken(request.refreshToken());

      List<String> authorities = List.of(ROLE);
      String newAccessToken = jwtTokenProvider.generateAccessToken(username, authorities);

      String newRefreshToken = jwtTokenProvider.isRefreshTokenExpiringSoon(request.refreshToken())
          ? jwtTokenProvider.generateRefreshToken(username)
          : request.refreshToken();

      return new LoginResponse(newAccessToken, newRefreshToken);
    } else {
      throw new InvalidTokenException("Invalid refresh token");
    }
  }

  @Override
  public UserCredentialsDataRecovery deleteUser(String userId) {
    UserCredentials user = findUserById(userId);

    List<UserRoleDTO> userRoles = userRoleRepository.findAllByUserLogin(user.getLogin())
        .stream()
        .map(userRoleMapper::toUserRoleDTO)
        .toList();

    userCredentialsRepository.delete(user);

    return new UserCredentialsDataRecovery(
        userId,
        user.getPasswordHash(),
        user.getLogin(),
        user.getCreatedAt(),
        user.getUpdatedAt(),
        userRoles
    );
  }

  @Override
  public UserCredentials findUserById(String userId) {
    return userCredentialsRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND + userId));
  }
}