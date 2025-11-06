package com.innowise.apigateway.dto.order;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Represents a request to create a new order
 */
 public record CreateOrderRequest(
    @NotNull String userId,
    List<OrderItemRequest> items
) {

}