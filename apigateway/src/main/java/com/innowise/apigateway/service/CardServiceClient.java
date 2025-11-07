package com.innowise.apigateway.service;

import com.innowise.apigateway.config.ServiceConfig;
import com.innowise.apigateway.dto.card.CardDTO;
import com.innowise.apigateway.dto.card.CreateCardRequest;
import com.innowise.apigateway.dto.card.UpdateCardRequest;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CardServiceClient {

  private final WebClient webClient;
  private final ServiceConfig serviceConfig;

  public CardServiceClient(WebClient webClient, ServiceConfig serviceConfig) {
    this.webClient = webClient;
    this.serviceConfig = serviceConfig;
  }

  public Mono<Void> deleteCardInCardService(Long id) {
    return webClient.delete()
        .uri(serviceConfig.getCardServiceUrl() + "/api/v1/cards/{id}", id)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnError(error ->
            log.error("Failed to delete card by id in CardService: {}", error.getMessage()));
  }

  public Mono<CardDTO> createCardInCardService(String userId, CreateCardRequest request) {
    CreateCardRequest cardRequest = new CreateCardRequest(
        request.number(), request.holder(), request.expirationDate());
    return webClient.post()
        .uri(serviceConfig.getCardServiceUrl() + "/api/v1/cards?userId={userId}", userId)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(cardRequest)
        .retrieve()
        .bodyToMono(CardDTO.class)
        .doOnError(error ->
            log.error("Failed to create card in CardService: {}", error.getMessage()));
  }

  public Mono<CardDTO> updateCardInCardService(Long id, UpdateCardRequest request) {
    return webClient.patch()
        .uri(serviceConfig.getCardServiceUrl() + "/api/v1/cards/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(CardDTO.class)
        .doOnError(error ->
            log.error("Failed to get cards by id in CardService: {}", error.getMessage()));
  }

  public Mono<List<CardDTO>> getCardsByIdsInCardService(List<Long> ids) {
    String fullUrl = serviceConfig.getCardServiceUrl() + "/api/v1/cards/batch?ids=" +
        ids.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));

    return webClient.get()
        .uri(fullUrl)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<CardDTO>>() {
        })
        .doOnError(error ->
            log.error("Failed to get cards by ids in CardService: {}", error.getMessage()));
  }

  public Mono<CardDTO> getCardByIdInCardService(Long id) {
    return webClient.get()
        .uri(serviceConfig.getCardServiceUrl() + "/api/v1/cards/{id}", id)
        .retrieve()
        .bodyToMono(CardDTO.class)
        .doOnError(error ->
            log.error("Failed to get card by id in CardService: {}", error.getMessage()));
  }

  public Mono<List<CardDTO>> getCardByUserIdInCardService(String id) {
    return webClient.get()
        .uri(serviceConfig.getCardServiceUrl() + "/api/v1/cards/user/{id}", id)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<CardDTO>>() {
        })
        .doOnError(error ->
            log.error("Failed to get cards by userId in CardService: {}", error.getMessage()));
  }
}
