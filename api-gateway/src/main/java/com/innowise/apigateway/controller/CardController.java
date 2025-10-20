package com.innowise.apigateway.controller;

import com.innowise.apigateway.dto.card.CardDTO;
import com.innowise.apigateway.dto.card.CreateCardRequest;
import com.innowise.apigateway.dto.card.UpdateCardRequest;
import com.innowise.apigateway.service.CardService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/cards")
public class CardController {

  private final CardService cardService;

  public CardController(CardService cardService) {
    this.cardService = cardService;
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<CardDTO>> getCardById(@PathVariable Long id) {
    return cardService.getCardById(id)
        .map(ResponseEntity::ok)
        .onErrorResume(error -> {
          log.error("Get card with id {} failed", id, error.getMessage());

          return Mono.just(ResponseEntity.badRequest().body(null));
        });
  }

  @GetMapping("/batch")
  public Mono<ResponseEntity<List<CardDTO>>> getCardsByIds(
      @RequestParam(name = "ids") List<Long> ids) {
    return cardService.getCardsByIds(ids)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.noContent().build())
        .onErrorResume(error -> {
          log.error("Cards not found with ids: {}", ids);

          return Mono.just(ResponseEntity.badRequest().body(null));
        });
  }

  @PatchMapping("/{id}")
  public Mono<ResponseEntity<CardDTO>> updateCard(
      @PathVariable Long id,
      @Valid @RequestBody UpdateCardRequest request) {

    return cardService.updateCard(id, request)
        .map(ResponseEntity::ok)
        .onErrorResume(error -> {
          log.error("Card was not updated with user id {}", id);

          return Mono.just(ResponseEntity.badRequest().body(null));
        });
  }

  @PostMapping
  public Mono<ResponseEntity<CardDTO>> createCard(@RequestParam(name = "userId") String userId,
      @Valid @RequestBody CreateCardRequest request) {
    return cardService.createCard(userId, request)
        .map(ResponseEntity::ok)
        .onErrorResume(error -> {
          log.error("Creation card failed for user with id {}: {}", userId, error.getMessage());

          return Mono.just(ResponseEntity.badRequest().body(null));
        });
  }

  @DeleteMapping("/{id}")
  public Mono<ResponseEntity<Void>> deleteCard(@PathVariable Long id) {
    return cardService.deleteCard(id)
        .map(v -> ResponseEntity.noContent().<Void>build())
        .onErrorResume(error -> {
          log.error("Delete failed for card with id {}: {}", id, error.getMessage());
          return Mono.just(ResponseEntity.badRequest().build());
        });
  }
}
