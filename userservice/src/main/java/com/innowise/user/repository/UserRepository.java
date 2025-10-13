package com.innowise.user.repository;

import com.innowise.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
  @Query(value = "SELECT * FROM users WHERE uuid IN :ids", nativeQuery = true)
  List<User> findByIdsIn(@Param("ids") List<String> ids);

  /**
   * Find a user by id
   */
  @Query(value = "SELECT * FROM users WHERE uuid = :id", nativeQuery = true)
  Optional<User> findById(@Param("id") String id);
}