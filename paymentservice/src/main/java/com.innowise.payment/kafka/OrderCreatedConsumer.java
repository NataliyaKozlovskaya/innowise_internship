package com.innowise.payment.kafka;

import com.innowise.payment.dto.kafka.OrderCreatedEvent;
import com.innowise.payment.properties.KafkaProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer component for handling Order Created events
 */
@Slf4j
@Component
public class OrderCreatedConsumer {

  private final PaymentProcessingService paymentProcessingService;
  private final KafkaProperties kafkaProperties;

  public OrderCreatedConsumer(PaymentProcessingService paymentProcessingService,
      KafkaProperties kafkaProperties) {
    this.paymentProcessingService = paymentProcessingService;
    this.kafkaProperties = kafkaProperties;
  }

  @KafkaListener(topics = "${spring.kafka.topics.order-created}", groupId = "payments-group")

  public void consumeOrderCreated(OrderCreatedEvent event) {
    try {
      log.info("Received order created event: orderId={}, userId={}, amount={}",
          event.getOrderId(), event.getUserId(), event.getAmount());

      paymentProcessingService.processOrderCreatedEvent(event);

      log.info("Successfully processed payment for order: {}", event.getOrderId());

    } catch (Exception e) {
      log.error("Failed to process order created event: orderId={}", event.getOrderId(), e);
    }
  }
}