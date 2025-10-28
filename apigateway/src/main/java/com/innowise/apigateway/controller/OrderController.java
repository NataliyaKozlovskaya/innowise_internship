package com.innowise.apigateway.controller;

import com.innowise.apigateway.dto.order.CreateOrderRequest;
import com.innowise.apigateway.enums.OrderStatus;
import com.innowise.apigateway.service.OrderService;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  public Mono<ServerResponse> createOrder(ServerRequest request) {
    return request.bodyToMono(CreateOrderRequest.class)
        .flatMap(orderService::createOrder)
        .flatMap(order -> ServerResponse.status(HttpStatus.CREATED).bodyValue(order))
        .onErrorResume(error -> {
          log.error("Creation order failed: {}", error.getMessage());
          return ServerResponse.badRequest().build();
        });
  }

  public Mono<ServerResponse> getOrderById(ServerRequest request) {
    Long id = Long.valueOf(request.pathVariable("id"));

    return orderService.getOrderById(id)
        .flatMap(order -> ServerResponse.ok().bodyValue(order))
        .onErrorResume(error -> {
          log.error("Get order with id {} failed", id, error.getMessage());
          return ServerResponse.badRequest().build();
        });
  }

  public Mono<ServerResponse> getOrdersByIds(ServerRequest request) {
    return Mono.justOrEmpty(request.queryParam("ids"))
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Ids parameter is required")))
        .map(ids -> Arrays.stream(ids.split(","))
            .map(String::trim)
            .map(Long::valueOf)
            .collect(Collectors.toList()))
        .flatMap(orderService::getOrdersByIds)
        .flatMap(orders -> {
          if (orders.isEmpty()) {
            return ServerResponse.noContent().build();
          }
          return ServerResponse.ok().bodyValue(orders);
        })
        .onErrorResume(error -> {
          if (error instanceof IllegalArgumentException) {
            log.warn("Ids parameter is missing");
            return ServerResponse.badRequest().bodyValue("Ids parameter is required");
          }
          if (error instanceof NumberFormatException) {
            log.warn("Invalid id format in request: {}", request.queryParam("ids").orElse(""));
            return ServerResponse.badRequest().bodyValue("Invalid id format");
          }
          log.error("Orders not found with ids: {}", request.queryParam("ids").orElse(""), error);
          return ServerResponse.badRequest().build();
        });
  }

  public Mono<ServerResponse> getOrdersByStatuses(ServerRequest request) {
    return Mono.justOrEmpty(request.queryParam("statuses"))
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Statuses parameter is required")))
        .map(statuses -> Arrays.stream(statuses.split(","))
            .map(String::trim)
            .map(OrderStatus::valueOf)
            .collect(Collectors.toList()))
        .flatMap(orderService::getOrdersByStatuses)
        .flatMap(orders -> ServerResponse.ok().bodyValue(orders))
        .switchIfEmpty(ServerResponse.ok().bodyValue(Collections.emptyList()))
        .onErrorResume(error -> {
          if (error instanceof IllegalArgumentException) {
            log.warn("Statuses parameter is missing");
            return ServerResponse.badRequest().bodyValue("Statuses parameter is required");
          }
          if (error instanceof IllegalArgumentException && error.getMessage()
              .contains("No enum constant")) {
            log.warn("Invalid order status in request: {}",
                request.queryParam("statuses").orElse(""));
            return ServerResponse.badRequest().bodyValue("Invalid order status");
          }
          log.error("Orders not found with statuses: {}", request.queryParam("statuses").orElse(""),
              error);
          return ServerResponse.ok().bodyValue(Collections.emptyList());
        });
  }

  public Mono<ServerResponse> updateOrderStatus(ServerRequest request) {
    Long id = Long.valueOf(request.pathVariable("id"));
    OrderStatus status = OrderStatus.valueOf(request.queryParam("status")
        .orElseThrow(() -> new IllegalArgumentException("Status parameter is required")));

    return orderService.updateOrder(id, status)
        .flatMap(order -> ServerResponse.ok().bodyValue(order))
        .onErrorResume(error -> {
          log.error("Order was not updated with id {}", id, error);
          return ServerResponse.badRequest().build();
        });
  }

  public Mono<ServerResponse> deleteOrder(ServerRequest request) {
    Long id = Long.valueOf(request.pathVariable("id"));

    return orderService.deleteOrder(id)
        .then(ServerResponse.noContent().build())
        .onErrorResume(error -> {
          log.error("Delete failed for order with id {}: {}", id, error.getMessage());
          return ServerResponse.noContent().build();
        });
  }
}