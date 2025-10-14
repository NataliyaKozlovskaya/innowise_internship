package com.innowise.order.dto;

/**
 * Represents a request for a single item within an order creation request
 */
 public record OrderItemRequest(
    Long itemId,
    Integer quantity
) {

}