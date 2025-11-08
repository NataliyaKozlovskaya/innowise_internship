package com.innowise.order.dto.kafka;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {

  private String orderId;
  private String userId;
  private BigDecimal amount;
  private LocalDateTime createdAt;
}