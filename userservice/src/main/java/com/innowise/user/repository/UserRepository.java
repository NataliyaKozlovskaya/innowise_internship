package com.innowise.user.repository;

import com.innowise.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for performing operations on {@link User} entities
 */
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Find a user by their unique email address
   */
  Optional<User> findByEmail(String email);

  /**
   * Find all users whose id is contained in the provided list
   */
  List<User> findByIdIn(List<Long> ids);
}