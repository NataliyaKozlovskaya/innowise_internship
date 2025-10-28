package com.innowise.user.repository;

import com.innowise.user.entity.Card;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for performing operations on {@link Card} entities
 */
public interface CardRepository extends JpaRepository<Card, Long> {

  /**
   * Find all cards whose id is contained in the provided list
   */
  List<Card> findByIdIn(List<Long> ids);

  /**
   * Find all user cards by user id
   */
  List<Card> findAllByUserUuid(String uuid);
}
