package com.innowise.payment.dto;

import com.innowise.payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Response object containing payment identification and status information
 */
@Getter
@Setter
@AllArgsConstructor
public class PaymentIdResponse {

  private String paymentId;
  private PaymentStatus status;
}
