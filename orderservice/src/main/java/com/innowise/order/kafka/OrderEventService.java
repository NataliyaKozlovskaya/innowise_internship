package com.innowise.order.kafka;


import com.innowise.order.dto.kafka.OrderCreatedEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventService {

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private static final String ORDER_CREATED_TOPIC = "order-created";

  public OrderEventService(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void sendOrderCreatedEvent(String orderId, String userId, BigDecimal amount, LocalDateTime createdAt) {
    OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, amount, createdAt);
    kafkaTemplate.send(ORDER_CREATED_TOPIC, orderId, event);
  }
}