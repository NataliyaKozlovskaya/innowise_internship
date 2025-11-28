package com.innowise.payment.dto;

import com.innowise.payment.enums.PaymentStatus;
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
