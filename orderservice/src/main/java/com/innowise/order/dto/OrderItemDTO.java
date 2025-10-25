package com.innowise.order.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object representing an item within an order for API responses
 */
public record OrderItemDTO(
    Long itemId,
    String itemName,
    BigDecimal itemPrice,
    Integer quantity
) {

}
