package com.java.project.authenticationservice.repository;

import com.java.project.authenticationservice.entity.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link UserCredentials} entities
 */
@Repository
public interface UserCredentialsRepository extends JpaRepository<UserCredentials, String> {

}