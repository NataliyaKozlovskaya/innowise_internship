package com.innowise.apigateway.dto.order;

import jakarta.validation.constraints.NotNull;

/**
 * Represents a request for a single item within an order creation request
 */
public record OrderItemRequest(
    @NotNull Long itemId,
    @NotNull Integer quantity
) {

}