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
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class OrderService {

  private final WebClient webClient;
  private final ServiceConfig serviceConfig;


  public OrderService(WebClient webClient, ServiceConfig serviceConfig) {
    this.webClient = webClient;
    this.serviceConfig = serviceConfig;
  }

  public Mono<OrderDTO> createOrder(CreateOrderRequest request) {
    log.info("API Gateway: Starting create order for user with id : {}", request.userId());

    return createOrderInOrderService(request)
        .flatMap(orderResponse -> {
          log.info("Order created successfully for user: {}", request.userId());
          return Mono.just(new OrderDTO(orderResponse.userId(), orderResponse.status(),
              orderResponse.creationDate(), orderResponse.orderItems()));
        })
        .onErrorResume(error -> {
          log.error("API Gateway: Creation order failed", error.getMessage());
          return Mono.error(new RuntimeException("Creation of order failed", error));
        });
  }

  public Mono<OrderDTO> getOrderById(Long id) {
    log.info("API Gateway: Starting find order in OrderService: {}", id);
    return getOrderByIdInOrderService(id)
        .flatMap(orderDTO -> {
          log.info("API Gateway: get order by id {} successful", id);
          return Mono.just(
              new OrderDTO(orderDTO.userId(), orderDTO.status(), orderDTO.creationDate(),
                  orderDTO.orderItems()));
        })
        .onErrorResume(error -> {
          log.error("API Gateway: Get order by id failed", error.getMessage());
          return Mono.error(new RuntimeException("Get order by id failed", error));
        });
  }

  public Mono<List<OrderDTO>> getOrdersByIds(List<Long> ids) {
    log.info("API Gateway: Starting find orders in OrderService: {}", ids);
    return getOrdersByIdsInOrderService(ids)
        .doOnSuccess(orderDTOs ->
            log.info("API Gateway: get orders by ids {} successful. Found {} orders", ids,
                orderDTOs.size()))
        .map(orderDTOs -> orderDTOs.stream()
            .map(order -> new OrderDTO(order.userId(), order.status(), order.creationDate(),
                order.orderItems()))
            .toList()
        )
        .onErrorResume(error -> {
          log.error("API Gateway: Get orders by ids failed", error.getMessage());
          return Mono.error(new RuntimeException("Get orders by ids failed", error));
        });
  }

  public Mono<List<OrderDTO>> getOrdersByStatuses(List<OrderStatus> statuses) {
    log.info("API Gateway: Starting find orders in OrderService: {}", statuses);
    return getOrdersByStatusesInOrderService(statuses)
        .doOnSuccess(orderDTOs ->
            log.info("API Gateway: get orders by ids {} successful. Found {} orders", statuses,
                orderDTOs.size()))
        .map(orderDTOs -> orderDTOs.stream()
            .map(order -> new OrderDTO(order.userId(), order.status(), order.creationDate(),
                order.orderItems()))
            .toList()
        )
        .onErrorResume(error -> {
          log.error("API Gateway: Get orders by statuses failed", error.getMessage());
          return Mono.error(new RuntimeException("Get orders by statuses failed", error));
        });
  }

  public Mono<OrderDTO> updateOrder(Long id, OrderStatus status) {
    log.info("API Gateway: Starting update order with id {} in OrderService: {}", id);
    return updateOrderInOrderService(id, status)
        .flatMap(orderDTO -> {
          log.info("API Gateway: update order by id {} successful", id);
          return Mono.just(
              new OrderDTO(orderDTO.userId(), orderDTO.status(), orderDTO.creationDate(),
                  orderDTO.orderItems()));
        })
        .onErrorResume(error -> {
          log.error("API Gateway: Update order failed", error.getMessage());
          return Mono.error(new RuntimeException("Update order failed", error));
        });
  }

  public Mono<Void> deleteOrder(Long id) {
    log.info("API Gateway: Starting delete order with id {}", id);

    return deleteOrderInCardService(id)
        .doOnSuccess(v -> log.info("API Gateway: Order deleted successfully: {}", id))
        .doOnError(error -> log.error("API Gateway: Delete order failed: {}", error.getMessage()));
  }

  private Mono<List<OrderDTO>> getOrdersByStatusesInOrderService(List<OrderStatus> statuses) {
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

  private Mono<Void> deleteOrderInCardService(Long id) {
    return webClient.delete()
        .uri(serviceConfig.getOrderServiceUrl() + "/api/v1/orders/{id}", id)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnError(error ->
            log.error("Failed to delete order by id in OrderService: {}", error.getMessage()));
  }

  private Mono<OrderDTO> updateOrderInOrderService(Long id, OrderStatus status) {
    return webClient.patch()
        .uri(serviceConfig.getOrderServiceUrl() + "/api/v1/orders/{id}/status?status={status}", id,
            status)
        .contentType(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(OrderDTO.class)
        .doOnError(error ->
            log.error("Failed to update orders by id in OrderService: {}", error.getMessage()));
  }

  private Mono<List<OrderDTO>> getOrdersByIdsInOrderService(List<Long> ids) {
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

  private Mono<OrderDTO> getOrderByIdInOrderService(Long id) {
    return webClient.get()
        .uri(serviceConfig.getOrderServiceUrl() + "/api/v1/orders/{id}", id)
        .retrieve()
        .bodyToMono(OrderDTO.class)
        .doOnError(
            error -> log.error("Failed to get order by id in OrderService: {}",
                error.getMessage()));
  }

  private Mono<OrderDTO> createOrderInOrderService(CreateOrderRequest request) {
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
