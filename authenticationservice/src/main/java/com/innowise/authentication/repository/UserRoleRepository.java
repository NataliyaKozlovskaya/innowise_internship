package com.innowise.authentication.repository;

import com.innowise.authentication.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link UserRole} entities
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

}