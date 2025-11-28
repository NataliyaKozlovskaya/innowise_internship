package com.innowise.apigateway.dto.payment;

import com.innowise.apigateway.enums.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO representing a Payment for API responses
 */
public record PaymentDTO (
    @NotNull Long orderId,
    @NotBlank String userId,
    @NotNull PaymentStatus status,
    @NotNull BigDecimal paymentAmount
){

}