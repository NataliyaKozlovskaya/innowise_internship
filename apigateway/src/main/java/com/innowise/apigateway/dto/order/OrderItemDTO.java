package com.innowise.apigateway.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Data Transfer Object representing an item within an order for API responses
 */
public record OrderItemDTO(
    @NotNull Long itemId,
    @NotBlank String itemName,
    @NotNull BigDecimal itemPrice,
    @NotNull Integer quantity
) {

}
