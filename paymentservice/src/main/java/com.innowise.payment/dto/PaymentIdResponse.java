package com.innowise.payment.dto;

import com.innowise.payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 *
 */
@Getter
@Setter
@AllArgsConstructor
public class PaymentIdResponse {

  private String paymentId;
  private PaymentStatus status;
}
