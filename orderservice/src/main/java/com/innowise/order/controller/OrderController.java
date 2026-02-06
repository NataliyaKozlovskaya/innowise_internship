package com.innowise.order.controller;

import com.innowise.order.dto.CreateOrderRequest;
import com.innowise.order.dto.OrderDTO;
import com.innowise.order.dto.kafka.OrderCreatedEvent;
import com.innowise.order.dto.kafka.PaymentProcessedEvent;
import com.innowise.order.enums.OrderStatus;
import com.innowise.order.service.OrderService;

import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing order operations.
 * Provides endpoints for creating, retrieving, updating, and deleting orders.
 */
 @RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping
  public ResponseEntity<OrderDTO> createOrder(@RequestBody CreateOrderRequest request) {
    OrderDTO order = orderService.createOrder(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(order);
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
    OrderDTO order = orderService.getOrderById(id);
    return ResponseEntity.ok(order);
  }

  @GetMapping("/batch")
  public ResponseEntity<List<OrderDTO>> getOrdersByIds(@RequestParam(name = "ids") List<Long> ids) {
    List<OrderDTO> orders = orderService.getOrdersByIds(ids);
    return ResponseEntity.ok(orders);
  }

  @GetMapping("/status")
  public ResponseEntity<List<OrderDTO>> getOrdersByStatuses(
      @RequestParam List<OrderStatus> statuses) {
    List<OrderDTO> orders = orderService.getOrdersByStatuses(statuses);
    return ResponseEntity.ok(orders);
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable Long id,
      @RequestParam OrderStatus status) {
    OrderDTO order = orderService.updateOrder(id, status);
    return ResponseEntity.ok(order);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
    orderService.deleteOrder(id);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/user/{id}")
  public ResponseEntity<Void> deleteOrderByUserId(@PathVariable String id) {
    orderService.deleteOrderByUserId(id);
    return ResponseEntity.noContent().build();
  }

    @GetMapping("/users/internal/{userId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByUserId(@PathVariable String userId) {
        List<OrderDTO> order = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(order);
    }
}
