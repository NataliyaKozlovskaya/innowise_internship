package com.innowise.apigateway.handler;

import com.innowise.apigateway.dto.card.CreateCardRequest;
import com.innowise.apigateway.dto.card.UpdateCardRequest;
import com.innowise.apigateway.manager.CardOperationManager;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Spring WebFlux controller handling card-related HTTP endpoints
 */
@Slf4j
@Component
public class CardHandler {

  private final CardOperationManager cardOperationManager;

  public CardHandler(CardOperationManager cardOperationManager) {
    this.cardOperationManager = cardOperationManager;
  }

  public Mono<ServerResponse> getCardById(ServerRequest request) {
    Long id = Long.valueOf(request.pathVariable("id"));

    return cardOperationManager.getCardById(id)
        .flatMap(card -> ServerResponse.ok().bodyValue(card))
        .onErrorResume(error -> {
          log.error("Get card with id {} failed", id, error.getMessage());
          return ServerResponse.badRequest().build();
        });
  }

  public Mono<ServerResponse> getCardByUserId(ServerRequest request) {
    String id = request.pathVariable("id");

    return cardOperationManager.getCardByUserId(id)
        .flatMap(cards -> ServerResponse.ok().bodyValue(cards))
        .onErrorResume(error -> {
          log.error("Get card with user id {} failed", id, error.getMessage());
          return ServerResponse.badRequest().build();
        });
  }

  public Mono<ServerResponse> getCardsByIds(ServerRequest request) {
    try {
      List<String> idStrings = request.queryParams().get("ids");

      if (idStrings == null || idStrings.isEmpty()) {
        return ServerResponse.badRequest().bodyValue("Ids parameter is required");
      }

      List<Long> ids = idStrings.stream()
          .flatMap(str -> Arrays.stream(str.split(",")))
          .map(String::trim)
          .map(Long::valueOf)
          .toList();

      return cardOperationManager.getCardsByIds(ids)
          .flatMap(cards -> {
            if (cards.isEmpty()) {
              return ServerResponse.noContent().build();
            }
            return ServerResponse.ok().bodyValue(cards);
          })
          .onErrorResume(error -> {
            log.error("Cards not found with ids: {}", ids, error);
            return ServerResponse.badRequest().build();
          });

    } catch (NumberFormatException e) {
      log.warn("Invalid id format in request");
      return ServerResponse.badRequest().bodyValue("Invalid id format");
    }
  }

  public Mono<ServerResponse> updateCard(ServerRequest request) {
    Long id = Long.valueOf(request.pathVariable("id"));

    return request.bodyToMono(UpdateCardRequest.class)
        .flatMap(updateRequest -> cardOperationManager.updateCard(id, updateRequest))
        .flatMap(card -> ServerResponse.ok().bodyValue(card))
        .onErrorResume(error -> {
          log.error("Card was not updated with id {}", id, error);
          return ServerResponse.badRequest().build();
        });
  }

  public Mono<ServerResponse> createCard(ServerRequest request) {
    String userId = request.queryParam("userId")
        .orElseThrow(() -> new IllegalArgumentException("UserId parameter is required"));

    return request.bodyToMono(CreateCardRequest.class)
        .flatMap(createRequest -> cardOperationManager.createCard(userId, createRequest))
        .flatMap(card -> ServerResponse.ok().bodyValue(card))
        .onErrorResume(error -> {
          log.error("Creation card failed for user with id {}: {}", userId, error.getMessage());
          return ServerResponse.badRequest().build();
        });
  }

  public Mono<ServerResponse> deleteCard(ServerRequest request) {
    Long id = Long.valueOf(request.pathVariable("id"));

    return cardOperationManager.deleteCard(id)
        .then(ServerResponse.noContent().build())
        .onErrorResume(error -> {
          log.error("Delete failed for card with id {}: {}", id, error.getMessage());
          return ServerResponse.badRequest().build();
        });
  }
}
