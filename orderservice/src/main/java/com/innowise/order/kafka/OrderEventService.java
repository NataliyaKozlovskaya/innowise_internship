package com.innowise.order.kafka;


import com.innowise.order.dto.kafka.OrderCreatedEvent;
import com.innowise.order.service.OrderService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderEventService {

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private static final String ORDER_CREATED_TOPIC = "order-created";
  private final OrderService orderService;

  public OrderEventService(KafkaTemplate<String, Object> kafkaTemplate, OrderService orderService) {
    this.kafkaTemplate = kafkaTemplate;
    this.orderService = orderService;
  }

  public void sendOrderCreatedEvent(Long orderId, String userId, BigDecimal amount,
      LocalDateTime createdAt) {
    OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, amount, createdAt);

    log.info("Send event order_created_event to paymentService with orderId : {}", orderId);
    kafkaTemplate.send(ORDER_CREATED_TOPIC, String.valueOf(orderId), event);
  }

  public void processPaymentEvent(Long orderId, String paymentStatus) {
    orderService.updateOrderStatus(orderId, paymentStatus);
    log.info("Successfully updated order status: orderId={}, status={}", orderId, paymentStatus);
  }
}