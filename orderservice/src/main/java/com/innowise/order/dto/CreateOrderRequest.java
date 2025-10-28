package com.innowise.order.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Represents a request to create a new order
 */
 public record CreateOrderRequest(
    @NotBlank String userId,
    List<OrderItemRequest> items
) {

}