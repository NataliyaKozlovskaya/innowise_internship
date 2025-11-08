package com.innowise.order.kafka;


import com.innowise.order.dto.kafka.PaymentProcessedEvent;
import com.innowise.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentProcessedConsumer {

  private static final Logger logger = LoggerFactory.getLogger(PaymentProcessedConsumer.class);

  private final OrderService orderService;

  public PaymentProcessedConsumer(OrderService orderService) {
    this.orderService = orderService;
  }

  @KafkaListener(
      topics = "order-payment-processed",
      groupId = "orders-group",
      containerFactory = "paymentProcessedKafkaListenerContainerFactory"
  )
  public void consumePaymentProcessed(PaymentProcessedEvent event) {
    try {
      logger.info("Received payment processed event: orderId={}, status={}",
          event.getOrderId(), event.getStatus());

      // Обновляем статус заказа на основе результата платежа
      orderService.updateOrderStatus(event.getOrderId(), event.getStatus());

      logger.info("Successfully updated order status: orderId={}, status={}",
          event.getOrderId(), event.getStatus());

    } catch (Exception e) {
      logger.error("Failed to process payment processed event: orderId={}",
          event.getOrderId(), e);
    }
  }
}