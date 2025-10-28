package com.innowise.authentication.repository;

import com.innowise.authentication.entity.UserRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link UserRole} entities
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

  /**
   * Find user roles
   *
   * @param login user login
   * @return list of user role
   */
  List<UserRole> findAllByUserLogin(String login);
}