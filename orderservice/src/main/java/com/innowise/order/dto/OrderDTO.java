package com.innowise.order.dto;

import com.innowise.order.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object representing an Order for API responses
 */
public record OrderDTO(
    String userId,
    OrderStatus status,
    LocalDateTime creationDate,
    List<OrderItemDTO> orderItems
) {

}