package com.java.project.controller;

import com.java.project.dto.card.CardDTO;
import com.java.project.dto.card.CreateCardRequest;
import com.java.project.dto.card.UpdateCardRequest;
import com.java.project.service.CardService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing Card operations. Provides endpoints for CRUD operations on Card
 * entities
 */
@RestController
@RequestMapping("/api/cards")
@Validated
public class CardController {
  private final CardService cardService;

  @Autowired
  public CardController(CardService cardService) {
    this.cardService = cardService;
  }

  /**
   * Create a new card
   */
  @PostMapping
  public ResponseEntity<CardDTO> createCard(@RequestParam (name = "userId") Long userId,
      @Valid @RequestBody CreateCardRequest request) {
    CardDTO createdCard = cardService.createCard(userId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
  }

  /**
   * Get card by ID
   */
  @GetMapping("/{id}")
  public ResponseEntity<CardDTO> getCardById(@PathVariable Long id) {
    CardDTO response = cardService.getCardById(id);
    return ResponseEntity.ok(response);
  }

  /**
   * Get cards by list of IDs
   */
  @GetMapping("/batch")
  public ResponseEntity<List<CardDTO>> getCardsByIds(@RequestParam(name = "ids") List<Long> ids) {
    List<CardDTO> cards = cardService.getCardsByIds(ids);
    return ResponseEntity.ok(cards);
  }

  /**
   * Update card information
   */
  @PatchMapping("/{id}")
  public ResponseEntity<CardDTO> updateCard(
      @PathVariable Long id,
      @Valid @RequestBody UpdateCardRequest request) {
    CardDTO updatedCard = cardService.updateCard(id, request);
    return ResponseEntity.ok(updatedCard);
  }

  /**
   * Delete card by ID
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
    cardService.deleteCard(id);
    return ResponseEntity.noContent().build();
  }
}