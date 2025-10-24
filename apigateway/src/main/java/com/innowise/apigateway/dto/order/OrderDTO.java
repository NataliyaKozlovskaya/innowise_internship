package com.innowise.apigateway.dto.order;

import com.innowise.apigateway.enums.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object representing an Order for API responses
 */
public record OrderDTO(
    @NotBlank String userId,
    @NotNull OrderStatus status,
    @NotNull LocalDateTime creationDate,
    List<OrderItemDTO> orderItems
) {

}