package com.innowise.order.kafka;


import com.innowise.order.dto.kafka.PaymentProcessedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer component for processing Payment Processed events
 */
@Slf4j
@Component
public class PaymentProcessedConsumer {

  private final OrderEventService orderEventService;

  public PaymentProcessedConsumer(OrderEventService orderEventService) {
    this.orderEventService = orderEventService;
  }

  @KafkaListener(
      topics = "order-payment-processed",
      groupId = "orders-group",
      containerFactory = "paymentProcessedKafkaListenerContainerFactory"
  )
  public void consumePaymentProcessed(PaymentProcessedEvent event) {
    try {
      log.info("Received payment processed event: orderId={}, status={}",
          event.getOrderId(), event.getStatus());

      orderEventService.processPaymentEvent(event.getOrderId(), event.getStatus());
    } catch (Exception e) {
      log.error("Failed to process payment processed event: orderId={}",
          event.getOrderId(), e);
    }
  }
}