package com.innowise.apigateway.service;

import com.innowise.apigateway.config.ServiceConfig;
import com.innowise.apigateway.dto.card.CardDTO;
import com.innowise.apigateway.dto.card.CreateCardRequest;
import com.innowise.apigateway.dto.card.UpdateCardRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class CardService {

  private final WebClient webClient;
  private final ServiceConfig serviceConfig;

  public CardService(WebClient webClient, ServiceConfig serviceConfig) {
    this.webClient = webClient;
    this.serviceConfig = serviceConfig;
  }


  public Mono<CardDTO> getCardById(Long id) {
    log.info("API Gateway: Starting find card in CardService: {}", id);

    return getCardByIdInCardService(id)
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

  public Mono<List<CardDTO>> getCardsByIds(List<Long> ids) {
    log.info("API Gateway: Starting find cards in CardService: {}", ids);

    return getCardsByIdsInCardService(ids)
        .flatMap(cardDTO -> {
          log.info("API Gateway: get card by id {} successful", ids);
          return Mono.just(
              List.of(new CardDTO(cardDTO.number(), cardDTO.holder(), cardDTO.expirationDate())));
        })
        .onErrorResume(error -> {
          log.error("API Gateway: Get cards by ids failed", error.getMessage());
          return Mono.error(new RuntimeException("Get cards by ids failed", error));
        });
  }

  public Mono<CardDTO> updateCard(Long id, UpdateCardRequest request) {
    log.info("API Gateway: Starting update card with user id {} in CardService: {}", id);
    return updateCardInCardService(id, request)
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

  public Mono<CardDTO> createCard(String userId, CreateCardRequest request) {
    log.info("API Gateway: Starting create card for user with id : {}", userId);

    return createCardInCardService(userId, request)
        .flatMap(cardResponse -> {
          log.info("Card created successfully for user: {}", userId);
          return Mono.just(new CardDTO(cardResponse.number(), cardResponse.holder(),
              cardResponse.expirationDate()));

        })
        .onErrorResume(error -> {
          log.error("API Gateway: Creation og card failed", error.getMessage());
          return Mono.error(new RuntimeException("Creation of card failed", error));
        });
  }

  public Mono<Void> deleteCard(Long id) {
    log.info("API Gateway: Starting delete card with id {}", id);

    return deleteCardInCardService(id)
        .doOnSuccess(v -> log.info("API Gateway: Card deleted successfully: {}", id))
        .doOnError(error -> log.error("API Gateway: Delete card failed: {}", error.getMessage()));
  }

  private Mono<Void> deleteCardInCardService(Long id) {
    return webClient.delete()
        .uri(serviceConfig.getCardServiceUrl() + "/api/v1/cards")
        .retrieve()
        .bodyToMono(Void.class)
        .doOnError(
            error -> log.error("Failed to delete card by id in CardService: {}",
                error.getMessage()));
  }

  private Mono<CardDTO> createCardInCardService(String userId, CreateCardRequest request) {
    CreateCardRequest cardRequest = new CreateCardRequest(
        request.number(), request.holder(), request.expirationDate());
    return webClient.post()
        .uri(uriBuilder -> uriBuilder
            .path(serviceConfig.getCardServiceUrl() + "/api/v1/cards")
            .queryParam("userId", userId)
            .build())
        .bodyValue(cardRequest)
        .retrieve()
        .bodyToMono(CardDTO.class)
        .doOnError(
            error -> log.error("Failed to create card in CardService: {}", error.getMessage()));
  }

  private Mono<CardDTO> updateCardInCardService(Long id, UpdateCardRequest request) {
    return webClient.patch()
        .uri(serviceConfig.getCardServiceUrl() + "/api/v1/cards/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(CardDTO.class)
        .doOnError(
            error -> log.error("Failed to get cards by id in CardService: {}",
                error.getMessage()));
  }

  private Mono<CardDTO> getCardsByIdsInCardService(List<Long> ids) {
    return webClient.get()
        .uri(uriBuilder -> {
          UriBuilder builder = uriBuilder
              .path(serviceConfig.getCardServiceUrl() + "/api/v1/cards");

          for (Long id : ids) {
            builder = builder.queryParam("ids", id);
          }

          return builder.build();
        })

        .retrieve()
        .bodyToMono(CardDTO.class)
        .doOnError(
            error -> log.error("Failed to get cards by ids in CardService: {}",
                error.getMessage()));
  }

  private Mono<CardDTO> getCardByIdInCardService(Long id) {
    return webClient.get()
        .uri(serviceConfig.getCardServiceUrl() + "/api/v1/cards/{id}", id)

        .retrieve()
        .bodyToMono(CardDTO.class)
        .doOnError(
            error -> log.error("Failed to get card by id in CardService: {}", error.getMessage()));
  }
}
