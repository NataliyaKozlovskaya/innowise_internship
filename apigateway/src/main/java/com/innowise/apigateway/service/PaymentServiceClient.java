package com.innowise.apigateway.service;

import com.innowise.apigateway.config.ServiceConfig;
import com.innowise.apigateway.dto.payment.PaymentDTO;
import com.innowise.apigateway.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class PaymentServiceClient {

  private final WebClient webClient;
  private final ServiceConfig serviceConfig;

  public PaymentServiceClient(WebClient webClient, ServiceConfig serviceConfig) {
    this.webClient = webClient;
    this.serviceConfig = serviceConfig;
  }

  public Mono<List<PaymentDTO>> getPaymentByUserId(String userId) {
    return webClient.get()
        .uri(serviceConfig.getPaymentServiceUrl() + "/api/v1/payments/user/{userId}", userId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<PaymentDTO>>() {
        })
        .doOnError(error ->
            log.error("Failed to get payment by userId in PaymentService: {}", error.getMessage()));
  }

  public Mono<List<PaymentDTO>> getPaymentByOrderId(Long orderId) {
    return webClient.get()
        .uri(serviceConfig.getPaymentServiceUrl() + "/api/v1/payments/order/{orderId}", orderId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<PaymentDTO>>() {
        })
        .doOnError(error ->
            log.error("Failed to get payment by orderId in PaymentService: {}",
                error.getMessage()));
  }

  public Mono<List<PaymentDTO>> getPaymentsByStatuses(List<PaymentStatus> statuses) {
    String statusParams = statuses.stream()
        .map(Enum::name)
        .collect(Collectors.joining(","));

    String fullUrl = serviceConfig.getPaymentServiceUrl() +
        "/api/v1/payments/status?statuses=" + statusParams;

    return webClient.get()
        .uri(fullUrl)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<PaymentDTO>>() {
        })
        .doOnError(error ->
            log.error("Failed to get payments by statuses in PaymentService: {}",
                error.getMessage()));
  }

  public Mono<BigDecimal> getTotalSumOfPeriod(LocalDateTime start, LocalDateTime end) {
    String fullUrl = serviceConfig.getPaymentServiceUrl() +
        "/api/v1/payments/total?start=" + start + "&end=" + end;

    return webClient.get()
        .uri(fullUrl)
        .retrieve()
        .bodyToMono(BigDecimal.class)
        .doOnError(error ->
            log.error("Failed to get total sum of payments for period in PaymentService: {}",
                error.getMessage()));
  }
}
