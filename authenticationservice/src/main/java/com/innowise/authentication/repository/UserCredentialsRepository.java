package com.innowise.authentication.repository;

import com.innowise.authentication.entity.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link UserCredentials} entities
 */
@Repository
public interface UserCredentialsRepository extends JpaRepository<UserCredentials, String> {

}