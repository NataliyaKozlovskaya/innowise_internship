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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

  private static final String USER_NOT_FOUND = "User not found with id: ";
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Transactional
  @Override
  public UserDTO createUser(CreateUserRequest request) {
    userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
      throw new EmailAlreadyExistsException("Email already exists: " + request.getEmail());
    });

    User user = new User();
    user.setName(request.getName());
    user.setSurname(request.getSurname());
    user.setBirthDate(request.getBirthDate());
    user.setEmail(request.getEmail());
    User savedUser = userRepository.save(user);

    return userMapper.toUserDTO(savedUser);
  }

  @Transactional(readOnly = true)
  @Override
  public UserDTO getUserById(Long id) {
    return userRepository.findById(id)
        .map(userMapper::toUserDTO)
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
  @Override
  public UserDTO getUserByEmail(String email) {
    return userRepository.findByEmail(email)
        .map(userMapper::toUserDTO)
        .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
  }

  @Transactional
  @Override
  public UserDTO updateUser(Long id, UpdateUserRequest request) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND + id));

    Optional.ofNullable(request.getName()).ifPresent(user::setName);
    Optional.ofNullable(request.getSurname()).ifPresent(user::setSurname);
    Optional.ofNullable(request.getEmail()).ifPresent(user::setEmail);

    userRepository.save(user);
    return userMapper.toUserDTO(user);
  }

  @Transactional
  @Override
  public void deleteUser(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND + id));
    userRepository.delete(user);
  }
}
