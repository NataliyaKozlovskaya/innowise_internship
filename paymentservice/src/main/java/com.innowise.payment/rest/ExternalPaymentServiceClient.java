package com.innowise.payment.rest;

import static com.innowise.payment.enums.PaymentStatus.COMPLETED;
import static com.innowise.payment.enums.PaymentStatus.FAILED;


import com.innowise.payment.dto.PaymentIdResponse;
import com.innowise.payment.properties.ExternalServiceProperties;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Client component for communicating with external payment service
 */
@Slf4j
@Component
public class ExternalPaymentServiceClient {

  private final RestTemplate restTemplate;
  private final ExternalServiceProperties externalServiceProperties;

  public ExternalPaymentServiceClient(RestTemplate restTemplate,
      ExternalServiceProperties externalServiceProperties) {
    this.restTemplate = restTemplate;
    this.externalServiceProperties = externalServiceProperties;
  }

  /**
   * Generates a payment ID in an external service
   */
  @Retryable(
      retryFor = {HttpServerErrorException.ServiceUnavailable.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 2000, multiplier = 2)
  )
  public PaymentIdResponse generatePaymentId() {
    try {
      String url = externalServiceProperties.getUrl();
      log.info("Calling external service URL: {}", url);

      String response = restTemplate.getForObject(url, String.class);

      if (response == null || response.trim().isEmpty()) {
        throw new RuntimeException("Empty response from external service");
      }
      String cleanedResponse = response.trim();
      log.info("Cleaned response: '{}'", cleanedResponse);

      int paymentId = Integer.parseInt(cleanedResponse);
      log.info("SUCCESS: Received paymentId: {}", paymentId);
      boolean isSuccessful = paymentId % 2 == 0;

      return new PaymentIdResponse(Integer.toString(paymentId), isSuccessful ? COMPLETED : FAILED);

    } catch (Exception e) {
      log.error("Error while calling external service: ", e);
      throw new RuntimeException("Failed to call external payment service", e);
    }
  }

  /**
   * Fallback method when all retry attempts fail
   */
  @Recover
  public PaymentIdResponse recoverGeneratePaymentId(HttpServerErrorException e) {
    log.error("External service completely unavailable, using local generation");

    int localPaymentId = ThreadLocalRandom.current().nextInt(1, 1001);
    boolean isSuccessful = localPaymentId % 2 == 0;

    return new PaymentIdResponse(Integer.toString(localPaymentId),
        isSuccessful ? COMPLETED : FAILED);
  }
}