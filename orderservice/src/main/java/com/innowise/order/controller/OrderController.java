package com.innowise.order.controller;

import com.innowise.order.dto.CreateOrderRequest;
import com.innowise.order.dto.OrderDTO;
import com.innowise.order.enums.OrderStatus;
import com.innowise.order.service.impl.OrderServiceImpl;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

  private final OrderServiceImpl orderService;

  public OrderController(OrderServiceImpl orderService) {
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
  public ResponseEntity<List<OrderDTO>> getOrdersByIds(@RequestParam List<Long> ids) {
    List<OrderDTO> orders = orderService.getOrdersByIds(ids);
    return ResponseEntity.ok(orders);
  }

  @GetMapping("/by-status")
  public ResponseEntity<List<OrderDTO>> getOrdersByStatuses(
      @RequestParam List<OrderStatus> statuses) {
    List<OrderDTO> orders = orderService.getOrdersByStatuses(statuses);
    return ResponseEntity.ok(orders);
  }

  @PutMapping("/{id}/status")
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
}
