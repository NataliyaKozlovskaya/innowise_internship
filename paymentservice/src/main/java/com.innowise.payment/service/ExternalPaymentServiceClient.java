package com.innowise.payment.service;

import static com.innowise.payment.enums.PaymentStatus.COMPLETED;
import static com.innowise.payment.enums.PaymentStatus.FAILED;


import com.innowise.payment.dto.PaymentIdResponse;
import com.innowise.payment.properties.ExternalServiceProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

/**
 *
 */
@Slf4j
public class ExternalPaymentServiceClient {

  private final RestTemplate restTemplate;
  private final ExternalServiceProperties externalServiceProperties;


  public ExternalPaymentServiceClient(RestTemplate restTemplate,
      ExternalServiceProperties externalServiceProperties) {
    this.restTemplate = restTemplate;
    this.externalServiceProperties = externalServiceProperties;
  }

  /**
   * Generates a payment ID in an external service. If the ID is even, the payment is successful;
   * if it is odd, the payment is unsuccessful.
   */
  public PaymentIdResponse generatePaymentId() {
    log.info("");
    try {
      Integer paymentId = restTemplate.getForObject(
          externalServiceProperties.getExternalServiceUrl(), Integer.class);
      boolean isSuccessful = paymentId % 2 == 0;
      log.info("");
      return new PaymentIdResponse(paymentId.toString(), isSuccessful ? COMPLETED : FAILED);
    } catch (Exception e) {
      log.error("");
      throw new RuntimeException("Failed to call external payment service", e);
    }
  }
}
