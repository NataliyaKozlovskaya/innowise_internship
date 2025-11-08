package com.innowise.payment.kafka;

import com.innowise.payment.dto.kafka.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 *
 */
@Slf4j
@Component
public class OrderCreatedConsumer {

  private final PaymentProcessingService paymentProcessingService;

  public OrderCreatedConsumer(PaymentProcessingService paymentProcessingService) {
    this.paymentProcessingService = paymentProcessingService;
  }

  @KafkaListener(topics = "order-created", groupId = "payments-group")
  public void consumeOrderCreated(OrderCreatedEvent event) {
    try {
      log.info("Received order created event: orderId={}, userId={}, amount={}",
          event.getOrderId(), event.getUserId(), event.getAmount());

      paymentProcessingService.processOrderCreatedEvent(event);

      log.info("Successfully processed payment for order: {}", event.getOrderId());

    } catch (Exception e) {
      log.error("Failed to process order created event: orderId={}", event.getOrderId(), e);
      //  retry logic OR dead letter queue
    }
  }
}