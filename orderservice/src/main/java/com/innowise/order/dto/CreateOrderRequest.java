package com.innowise.order.dto;

import java.util.List;

/**
 * Represents a request to create a new order
 */
 public record CreateOrderRequest(
    String userId,
    List<OrderItemRequest> items
) {

}