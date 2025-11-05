package com.innowise.apigateway.manager;

import com.innowise.apigateway.dto.card.CardDTO;
import com.innowise.apigateway.dto.card.CreateCardRequest;
import com.innowise.apigateway.dto.card.UpdateCardRequest;
import com.innowise.apigateway.service.CardServiceClient;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CardOperationManager {

  private final CardServiceClient cardClient;


  public CardOperationManager(CardServiceClient cardClient) {
    this.cardClient = cardClient;
  }

  /**
   * Get card by id
   */
  public Mono<CardDTO> getCardById(Long id) {
    log.info("API Gateway: Starting find card in CardService: {}", id);

    return cardClient.getCardByIdInCardService(id)
        .flatMap(cardDTO -> {
          log.info("API Gateway: get card by id {} successful", id);
          return Mono.just(
              new CardDTO(cardDTO.number(), cardDTO.holder(), cardDTO.expirationDate()));
        })
        .onErrorResume(error -> {
          log.error("API Gateway: Get card by id failed", error.getMessage());
          return Mono.error(new RuntimeException("Get card by id failed", error));
        });
  }

  /**
   * Get card by userId
   */
  public Mono<List<CardDTO>> getCardByUserId(String id) {
    log.info("API Gateway: Starting find cards by userId in CardService: {}", id);

    return cardClient.getCardByUserIdInCardService(id)
        .doOnSuccess(
            cardDTOs -> log.info("API Gateway: get cards by userId {} successful. Found {} cards",
                id, cardDTOs.size()))
        .map(cardDTOs -> cardDTOs.stream()
            .map(card -> new CardDTO(card.number(), card.holder(), card.expirationDate()))
            .toList()
        )
        .onErrorResume(error -> {
          log.error("API Gateway: Get card by userId failed", error.getMessage());
          return Mono.error(new RuntimeException("Get card by userId failed", error));
        });
  }

  /**
   * Get list cards by ids
   */
  public Mono<List<CardDTO>> getCardsByIds(List<Long> ids) {
    log.info("API Gateway: Starting find cards in CardService: {}", ids);

    return cardClient.getCardsByIdsInCardService(ids)
        .doOnSuccess(cardDTOs ->
            log.info("API Gateway: get cards by ids {} successful. Found {} cards", ids,
                cardDTOs.size()))
        .map(cardDTOs -> cardDTOs.stream()
            .map(card -> new CardDTO(card.number(), card.holder(), card.expirationDate()))
            .toList()
        )
        .onErrorResume(error -> {
          log.error("API Gateway: Get cards by ids failed", error.getMessage());
          return Mono.error(new RuntimeException("Get cards by ids failed", error));
        });
  }

  /**
   * Update card
   */
  public Mono<CardDTO> updateCard(Long id, UpdateCardRequest request) {
    log.info("API Gateway: Starting update card with user id {} in CardService: {}", id);
    return cardClient.updateCardInCardService(id, request)
        .flatMap(cardDTO -> {
          log.info("API Gateway: update card by user id {} successful", id);
          return Mono.just(
              new CardDTO(cardDTO.number(), cardDTO.holder(), cardDTO.expirationDate()));
        })
        .onErrorResume(error -> {
          log.error("API Gateway: Update card failed", error.getMessage());
          return Mono.error(new RuntimeException("Update card failed", error));
        });
  }

  /**
   * Create card
   */
  public Mono<CardDTO> createCard(String userId, CreateCardRequest request) {
    log.info("API Gateway: Starting create card for user with id : {}", userId);

    return cardClient.createCardInCardService(userId, request)
        .flatMap(cardResponse -> {
          log.info("Card created successfully for user: {}", userId);
          return Mono.just(new CardDTO(cardResponse.number(), cardResponse.holder(),
              cardResponse.expirationDate()));

        })
        .onErrorResume(error -> {
          log.error("API Gateway: Creation card failed", error.getMessage());
          return Mono.error(new RuntimeException("Creation of card failed", error));
        });
  }

  /**
   * Delete card
   */
  public Mono<Void> deleteCard(Long id) {
    log.info("API Gateway: Starting delete card with id {}", id);

    return cardClient.deleteCardInCardService(id)
        .doOnSuccess(v -> log.info("API Gateway: Card deleted successfully: {}", id))
        .doOnError(error -> log.error("API Gateway: Delete card failed: {}", error.getMessage()));
  }
}
