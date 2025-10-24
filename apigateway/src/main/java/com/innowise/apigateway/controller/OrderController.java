package com.innowise.apigateway.controller;

import com.innowise.apigateway.dto.order.CreateOrderRequest;
import com.innowise.apigateway.dto.order.OrderDTO;
import com.innowise.apigateway.enums.OrderStatus;
import com.innowise.apigateway.service.OrderService;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/orders")
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping
  public Mono<ResponseEntity<OrderDTO>> createOrder(@RequestBody CreateOrderRequest request) {
    return orderService.createOrder(request)
        .map(order -> ResponseEntity.status(HttpStatus.CREATED).body(order))
        .onErrorResume(error -> {
          log.error("Creation card failed for user with id {}: {}", request.userId(),
              error.getMessage());

          return Mono.just(ResponseEntity.badRequest().body(null));
        });
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<OrderDTO>> getOrderById(@PathVariable Long id) {
    return orderService.getOrderById(id)
        .map(ResponseEntity::ok)
        .onErrorResume(error -> {
          log.error("Get card with id {} failed", id, error.getMessage());

          return Mono.just(ResponseEntity.badRequest().body(null));
        });
  }

  @GetMapping("/batch")
  public Mono<ResponseEntity<List<OrderDTO>>> getOrdersByIds(
      @RequestParam(name = "ids") List<Long> ids) {
    return orderService.getOrdersByIds(ids)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.noContent().build())
        .onErrorResume(error -> {
          log.error("Orders not found with ids: {}", ids);

          return Mono.just(ResponseEntity.badRequest().body(null));
        });
  }

  @GetMapping("/status")
  public Mono<ResponseEntity<List<OrderDTO>>> getOrdersByStatuses(
      @RequestParam List<OrderStatus> statuses) {
    return orderService.getOrdersByStatuses(statuses)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.ok(Collections.emptyList()))
        .onErrorResume(error -> {
          log.error("Orders not found with statuses: {}", statuses, error);
          return Mono.just(ResponseEntity.ok(Collections.emptyList()));
        });
  }

  @PatchMapping("/{id}/status")
  public Mono<ResponseEntity<OrderDTO>> updateOrderStatus(
      @PathVariable Long id,
      @RequestParam OrderStatus status) {

    return orderService.updateOrder(id, status)
        .map(ResponseEntity::ok)
        .onErrorResume(error -> {
          log.error("Order was not updated with id {}", id);

          return Mono.just(ResponseEntity.badRequest().body(null));
        });
  }

  @DeleteMapping("/{id}")
  public Mono<ResponseEntity<Void>> deleteOrder(@PathVariable Long id) {
    return orderService.deleteOrder(id)
        .map(v -> ResponseEntity.noContent().<Void>build())
        .onErrorResume(error -> {
          log.error("Delete failed for order with id {}: {}", id, error.getMessage());
          return Mono.just(ResponseEntity.noContent().build());
        });
  }
}