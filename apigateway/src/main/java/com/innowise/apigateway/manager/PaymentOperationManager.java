package com.innowise.apigateway.manager;

import com.innowise.apigateway.dto.payment.PaymentDTO;
import com.innowise.apigateway.enums.PaymentStatus;
import com.innowise.apigateway.service.PaymentServiceClient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class PaymentOperationManager {

  private final PaymentServiceClient paymentClient;

  public PaymentOperationManager(PaymentServiceClient paymentServiceClient) {
    this.paymentClient = paymentServiceClient;
  }

  /**
   * Get payment by user id
   */
  public Mono<List<PaymentDTO>> getPaymentByUserId(String userId) {
    log.info("API Gateway: Starting find payment by userId in PaymentService: {}", userId);
    return paymentClient.getPaymentByUserId(userId)
        .doOnSuccess(paymentDTOs ->
            log.info("API Gateway: get payment by userId {} successful. Found {} payments", userId,
                paymentDTOs.size()))
        .map(paymentDTOs -> paymentDTOs.stream()
            .map(payment -> new PaymentDTO(payment.orderId(), payment.userId(), payment.status(),
                payment.paymentAmount()))
            .toList())
        .onErrorResume(error -> {
          log.error("API Gateway: Get payment by userId failed", error.getMessage());
          return Mono.error(new RuntimeException("Get payment by userId failed", error));
        });
  }

  /**
   * Get payment by order id
   */
  public Mono<List<PaymentDTO>> getPaymentByOrderId(Long orderId) {
    log.info("API Gateway: Starting find payment by orderId in PaymentService: {}", orderId);
    return paymentClient.getPaymentByOrderId(orderId)
        .doOnSuccess(paymentDTOs ->
            log.info("API Gateway: get payment by orderId {} successful. Found {} payments",
                orderId,
                paymentDTOs.size()))
        .map(paymentDTOs -> paymentDTOs.stream()
            .map(payment -> new PaymentDTO(payment.orderId(), payment.userId(), payment.status(),
                payment.paymentAmount()))
            .toList())
        .onErrorResume(error -> {
          log.error("API Gateway: Get payment by orderId failed", error.getMessage());
          return Mono.error(new RuntimeException("Get payment by orderId failed", error));
        });
  }

  /**
   * Get payments by statuses
   */
  public Mono<List<PaymentDTO>> getPaymentByPaymentsStatuses(List<PaymentStatus> statuses) {
    log.info("API Gateway: Starting find s by statuses in PaymentService: {}", statuses);
    return paymentClient.getPaymentsByStatuses(statuses)
        .doOnSuccess(paymentDTOs ->
            log.info("API Gateway: get payments by statuses {} successful. Found {} payments",
                statuses,
                paymentDTOs.size()))
        .map(paymentDTOs -> paymentDTOs.stream()
            .map(payment -> new PaymentDTO(payment.orderId(), payment.userId(), payment.status(),
                payment.paymentAmount()))
            .toList())
        .onErrorResume(error -> {
          log.error("API Gateway: Get payments by statuses failed", error.getMessage());
          return Mono.error(new RuntimeException("Get payments by statuses failed", error));
        });
  }

  /**
   * Get total sum of payments for period
   */
  public Mono<BigDecimal> getTotalSumOfPeriod(LocalDateTime start, LocalDateTime end) {
    log.info("API Gateway: Starting get total sum of payments for period: {}, {}", start, end);
    return paymentClient.getTotalSumOfPeriod(start, end)
        .doOnSuccess(sum ->
            log.info("API Gateway: Get total sum of payments for period {}, {} is successful: {}",
                start, end, sum))
        .onErrorResume(error -> {
          log.error("API Gateway: Get total sum of payments for period failed: {}",
              error.getMessage());
          return Mono.error(
              new RuntimeException("Get total sum of payments for period failed", error));
        });
  }
}
