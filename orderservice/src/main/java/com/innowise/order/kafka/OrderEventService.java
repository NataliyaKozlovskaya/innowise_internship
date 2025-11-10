package com.innowise.order.kafka;


import com.innowise.order.dto.kafka.OrderCreatedEvent;
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

  public OrderEventService(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void sendOrderCreatedEvent(Long orderId, String userId, BigDecimal amount,
      LocalDateTime createdAt) {
    OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, amount, createdAt);

    log.info("Send event ORDER_CREATED_event to paymentService with orderId : ", orderId);
    kafkaTemplate.send(ORDER_CREATED_TOPIC, String.valueOf(orderId), event);
  }
}