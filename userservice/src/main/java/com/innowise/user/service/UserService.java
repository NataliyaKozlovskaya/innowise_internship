package com.innowise.user.service;

import com.innowise.user.dto.user.CreateUserRequest;
import com.innowise.user.dto.user.UpdateUserRequest;
import com.innowise.user.dto.user.UserDTO;
import com.innowise.user.entity.User;
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
  UserDTO getUserById(String id);

  /**
   * Find userEntity by identifier
   *
   * @param id user identifier
   * @return user
   */
  User getUserEntityById(String id);

  /**
   * Find list of users by ids
   *
   * @param ids list of  identifiers
   * @return list of users
   */
  List<UserDTO> getUsersByIds(List<String> ids);

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
  UserDTO updateUser(String id, UpdateUserRequest request);

  /**
   * Delete user by identifier (cascade delete cards)
   *
   * @param id user identifier
   */
  void deleteUser(String id);
}
