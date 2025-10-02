package com.java.project.service.impl;

import com.java.project.dto.user.CreateUserRequest;
import com.java.project.dto.user.UpdateUserRequest;
import com.java.project.dto.user.UserDTO;
import com.java.project.entity.User;
import com.java.project.exception.EmailAlreadyExistsException;
import com.java.project.exception.UserNotFoundException;
import com.java.project.repository.UserRepository;
import com.java.project.service.UserService;
import com.java.project.util.UserMapper;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@AllArgsConstructor
@CacheConfig(cacheNames = "users")
public class UserServiceImpl implements UserService {

  private static final String USER_NOT_FOUND = "User not found with id: ";
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Transactional
  @Override
  public UserDTO createUser(CreateUserRequest request) {
    userRepository.findByEmail(request.email()).ifPresent(user -> {
      throw new EmailAlreadyExistsException("Email already exists: " + request.email());
    });

    User user = new User();
    user.setName(request.name());
    user.setSurname(request.surname());
    user.setBirthDate(request.birthDate());
    user.setEmail(request.email());
    User savedUser = userRepository.save(user);

    return userMapper.toUserDTO(savedUser);
  }

  @Transactional(readOnly = true)
  @Cacheable(key = "#id", unless = "#result == null")
  @Override
  public UserDTO getUserById(Long id) {
    return userRepository.findById(id)
        .map(userMapper::toUserDTO)
        .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND + id));
  }

  @Transactional(readOnly = true)
  @Override
  public User getUserEntityById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND + id));
  }

  @Transactional(readOnly = true)
  @Override
  public List<UserDTO> getUsersByIds(List<Long> ids) {

    List<UserDTO> result = userRepository.findByIdIn(ids).stream()
        .map(userMapper::toUserDTO)
        .toList();

    if (result.isEmpty()) {
      throw new UserNotFoundException("Users not found with ids: " + ids);
    }
    return result;
  }

  @Transactional(readOnly = true)
  @Cacheable(key = "'email:' + #email", unless = "#result == null")
  @Override
  public UserDTO getUserByEmail(String email) {
    return userRepository.findByEmail(email)
        .map(userMapper::toUserDTO)
        .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
  }

  @Transactional
  @CachePut(key = "#id")
  @Override
  public UserDTO updateUser(Long id, UpdateUserRequest request) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND + id));

    if (request.email() != null && !request.email().equals(user.getEmail())) {
      evictEmailCache(user.getEmail());
    }

    Optional.ofNullable(request.name()).ifPresent(user::setName);
    Optional.ofNullable(request.surname()).ifPresent(user::setSurname);
    Optional.ofNullable(request.email()).ifPresent(user::setEmail);

    User updatedUser = userRepository.save(user);
    return userMapper.toUserDTO(updatedUser);
  }

  @Transactional
  @CacheEvict(key = "#id")
  @Override
  public void deleteUser(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND + id));

    evictEmailCache(user.getEmail());
    userRepository.delete(user);
  }

  /**
   * Invalidate the old cache via email
   */
  @CacheEvict(value = "users", key = "'email:' + #email")
  public void evictEmailCache(String email) {
    log.debug("Invalidated cache for email: {}", email);
  }
}
