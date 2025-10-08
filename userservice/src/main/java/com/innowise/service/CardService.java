package com.java.project.userservice.service;

import com.java.project.userservice.dto.card.CardDTO;
import com.java.project.userservice.dto.card.CreateCardRequest;
import com.java.project.userservice.dto.card.UpdateCardRequest;
import java.util.List;


/**
 * Service interface for managing Card entities. Provides comprehensive CRUD operations for users
 * and their payment cards.
 */
public interface CardService {

  /**
   * Creates a new card
   *
   * @param request card to be created
   * @return card
   */
  CardDTO createCard(Long userId, CreateCardRequest request);

  /**
   * Find card by identifier
   *
   * @param id card identifier
   * @return card
   */
  CardDTO getCardById(Long id);

  /**
   * Find list of card by ids
   *
   * @param ids list of  ids
   * @return list of Cards
   */
  List<CardDTO> getCardsByIds(List<Long> ids);


  /**
   * Update card by identifier
   *
   * @param id      user identifier
   * @param request the card object containing updated information
   * @return updated card
   */
  CardDTO updateCard(Long id, UpdateCardRequest request);

  /**
   * Delete card by identifier
   *
   * @param id card identifier
   */
  void deleteCard(Long id);
}
