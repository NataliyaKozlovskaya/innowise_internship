package com.innowise.payment.entity;

import com.innowise.payment.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Entity class representing a payment
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {

  @Id
  private String id;

  @Indexed
  @Field("order_id")
  private Long orderId;

  @Indexed
  @Field("user_id")
  private String userId;

  private PaymentStatus status;

  private LocalDateTime timestamp;

  @Field("payment_amount")
  private BigDecimal paymentAmount;

  @Override
  public String toString() {
    return String.format(
        "Payment[id=%s, orderId=%s, userId=%s, status=%s, amount=%s]",
        id, orderId, userId, status, paymentAmount);
  }
}
