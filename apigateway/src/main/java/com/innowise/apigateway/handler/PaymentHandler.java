package com.innowise.apigateway.handler;

import com.innowise.apigateway.enums.PaymentStatus;
import com.innowise.apigateway.manager.PaymentOperationManager;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Spring WebFlux handler handling payment-related HTTP endpoints
 */
@Slf4j
@Component
public class PaymentHandler {

  private final PaymentOperationManager paymentOperationManager;

  public PaymentHandler(PaymentOperationManager paymentOperationManager) {
    this.paymentOperationManager = paymentOperationManager;
  }

  public Mono<ServerResponse> getPaymentsByUserId(ServerRequest request) {
    String userId = request.pathVariable("userId");

    return paymentOperationManager.getPaymentByUserId(userId)
        .flatMap(payment -> ServerResponse.ok().bodyValue(payment))
        .onErrorResume(error -> {
          log.error("Get payment with userId {} failed", userId, error.getMessage());
          return ServerResponse.badRequest().build();
        });
  }

  public Mono<ServerResponse> getPaymentsByOrderId(ServerRequest request) {
    Long orderId = Long.valueOf(request.pathVariable("orderId"));

    return paymentOperationManager.getPaymentByOrderId(orderId)
        .flatMap(payment -> ServerResponse.ok().bodyValue(payment))
        .onErrorResume(error -> {
          log.error("Get payment with orderId {} failed", orderId, error.getMessage());
          return ServerResponse.badRequest().build();
        });
  }

  public Mono<ServerResponse> getPaymentsByPaymentStatuses(ServerRequest request) {
    return Mono.justOrEmpty(request.queryParam("statuses"))
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Statuses parameter is required")))
        .map(statuses -> Arrays.stream(statuses.split(","))
            .map(String::trim)
            .map(PaymentStatus::valueOf)
            .toList())
        .flatMap(paymentOperationManager::getPaymentByPaymentsStatuses)
        .flatMap(payments -> ServerResponse.ok().bodyValue(payments))
        .switchIfEmpty(ServerResponse.ok().bodyValue(Collections.emptyList()))
        .onErrorResume(error -> {
          if (error instanceof IllegalArgumentException) {
            log.warn("Statuses parameter is missing");
            return ServerResponse.badRequest().bodyValue("Statuses parameter is required");
          }
          if (error instanceof IllegalArgumentException && error.getMessage()
              .contains("No enum constant")) {
            log.warn("Invalid payment status in request: {}",
                request.queryParam("statuses").orElse(""));
            return ServerResponse.badRequest().bodyValue("Invalid payment status");
          }
          log.error("Payments were not found with statuses: {}", request.queryParam("statuses")
              .orElse(""), error);
          return ServerResponse.ok().bodyValue(Collections.emptyList());
        });
  }

  public Mono<ServerResponse> getTotalSumOfPaymentsForPeriod(ServerRequest request) {
    LocalDateTime startDate = LocalDateTime.parse(request.queryParam("start")
        .orElseThrow(() -> new IllegalArgumentException("Start parameter is required")));
    LocalDateTime endDate = LocalDateTime.parse(request.queryParam("end")
        .orElseThrow(() -> new IllegalArgumentException("End parameter is required")));

    return paymentOperationManager.getTotalSumOfPeriod(startDate, endDate)
        .flatMap(sum -> ServerResponse.ok().bodyValue(sum))
        .onErrorResume(error -> {
          log.error("Get total sum of payment for period {}, {} failed", startDate, endDate,
              error.getMessage());
          return ServerResponse.badRequest().build();
        });
  }
}
