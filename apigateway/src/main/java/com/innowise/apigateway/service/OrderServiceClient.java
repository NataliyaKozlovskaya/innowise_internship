package com.innowise.apigateway.service;

import com.innowise.apigateway.config.ServiceConfig;
import com.innowise.apigateway.dto.order.CreateOrderRequest;
import com.innowise.apigateway.dto.order.OrderDTO;
import com.innowise.apigateway.enums.OrderStatus;
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
public class OrderServiceClient {

  private final WebClient webClient;
  private final ServiceConfig serviceConfig;

  public OrderServiceClient(WebClient webClient, ServiceConfig serviceConfig) {
    this.webClient = webClient;
    this.serviceConfig = serviceConfig;
  }

  public Mono<List<OrderDTO>> getOrdersByStatusesInOrderService(List<OrderStatus> statuses) {
    String statusParams = statuses.stream()
        .map(Enum::name)
        .collect(Collectors.joining(","));

    String fullUrl = serviceConfig.getOrderServiceUrl() +
        "/api/v1/orders/status?statuses=" + statusParams;

    return webClient.get()
        .uri(fullUrl)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<OrderDTO>>() {
        })
        .doOnError(error ->
            log.error("Failed to get orders by statuses in OrderService: {}", error.getMessage()));
  }

  public Mono<Void> deleteOrderInCardService(Long id) {
    return webClient.delete()
        .uri(serviceConfig.getOrderServiceUrl() + "/api/v1/orders/{id}", id)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnError(error ->
            log.error("Failed to delete order by id in OrderService: {}", error.getMessage()));
  }

  public Mono<OrderDTO> updateOrderInOrderService(Long id, OrderStatus status) {
    return webClient.patch()
        .uri(serviceConfig.getOrderServiceUrl() + "/api/v1/orders/{id}/status?status={status}", id,
            status)
        .contentType(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(OrderDTO.class)
        .doOnError(error ->
            log.error("Failed to update orders by id in OrderService: {}", error.getMessage()));
  }

  public Mono<List<OrderDTO>> getOrdersByIdsInOrderService(List<Long> ids) {
    String fullUrl = serviceConfig.getOrderServiceUrl() + "/api/v1/orders/batch?ids=" +
        ids.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));

    return webClient.get()
        .uri(fullUrl)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<OrderDTO>>() {
        })
        .doOnError(error ->
            log.error("Failed to get orders by ids in OrderService: {}", error.getMessage()));
  }

  public Mono<OrderDTO> getOrderByIdInOrderService(Long id) {
    return webClient.get()
        .uri(serviceConfig.getOrderServiceUrl() + "/api/v1/orders/{id}", id)
        .retrieve()
        .bodyToMono(OrderDTO.class)
        .doOnError(
            error -> log.error("Failed to get order by id in OrderService: {}",
                error.getMessage()));
  }

  public Mono<OrderDTO> createOrderInOrderService(CreateOrderRequest request) {
    return webClient.post()
        .uri(serviceConfig.getOrderServiceUrl() + "/api/v1/orders")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(OrderDTO.class)
        .doOnError(
            error -> log.error("Failed to create order in OrderService: {}", error.getMessage()));
  }
}
