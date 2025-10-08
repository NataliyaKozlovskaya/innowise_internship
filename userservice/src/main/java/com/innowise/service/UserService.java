package com.java.project.userservice.service;

import com.java.project.userservice.dto.user.CreateUserRequest;
import com.java.project.userservice.dto.user.UpdateUserRequest;
import com.java.project.userservice.dto.user.UserDTO;
import com.java.project.userservice.entity.User;
import java.util.List;

/**
 * Service interface for managing User entities. Provides comprehensive CRUD operations for users
 * and their payment cards.
 */
public interface UserService {

  /**
   * Creates a new user
   *
   * @param request user to be created
   * @return user
   */
  UserDTO createUser(CreateUserRequest request);

  /**
   * Find user by identifier
   *
   * @param id user identifier
   * @return userDTO
   */
  UserDTO getUserById(Long id);

  /**
   * Find userEntity by identifier
   *
   * @param id user identifier
   * @return user
   */
  User getUserEntityById(Long id);

  /**
   * Find list of users by ids
   *
   * @param ids list of  identifiers
   * @return list of users
   */
  List<UserDTO> getUsersByIds(List<Long> ids);

  /**
   * Find user by email
   *
   * @param email user email
   * @return user
   */
  UserDTO getUserByEmail(String email);

  /**
   * Update user by identifier
   *
   * @param id      user identifier
   * @param request the user object containing updated information
   * @return updated user
   */
  UserDTO updateUser(Long id, UpdateUserRequest request);

  /**
   * Delete user by identifier (cascade delete cards)
   *
   * @param id user identifier
   */
  void deleteUser(Long id);
}
