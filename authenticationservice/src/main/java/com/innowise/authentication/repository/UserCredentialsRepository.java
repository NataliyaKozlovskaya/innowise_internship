package com.innowise.authentication.repository;

import com.innowise.authentication.entity.UserCredentials;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link UserCredentials} entities
 */
@Repository
public interface UserCredentialsRepository extends JpaRepository<UserCredentials, String> {

  /**
   * Find user info by login
   */
  @Query("SELECT uc FROM UserCredentials uc LEFT JOIN FETCH uc.roles WHERE uc.login = :login")
  Optional<UserCredentials> findByLoginWithRoles(@Param("login") String login);
}