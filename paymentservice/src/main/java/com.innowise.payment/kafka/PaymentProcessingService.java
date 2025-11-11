package com.innowise.payment.kafka;


import static com.innowise.payment.enums.PaymentStatus.FAILED;

import com.innowise.payment.dto.PaymentDTO;
import com.innowise.payment.dto.PaymentIdResponse;
import com.innowise.payment.dto.kafka.OrderCreatedEvent;
import com.innowise.payment.dto.kafka.PaymentProcessedEvent;
import com.innowise.payment.entity.Payment;
import com.innowise.payment.enums.PaymentStatus;
import com.innowise.payment.service.ExternalPaymentServiceClient;
import com.innowise.payment.service.PaymentService;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Payment processing service. Enables interaction with external payment systems, stores payment
 * information and sends events to Kafka.
 */
@Slf4j
@Service
public class PaymentProcessingService {

  private final PaymentService paymentService;
  private final ExternalPaymentServiceClient externalPaymentService;
  private final KafkaTemplate<String, Object> kafkaTemplate;

  private static final String ORDER_PROCESSED_TOPIC = "order-payment-processed";
  private static final String PAYMENT_FAILED_TOPIC = "payment-failed";

  public PaymentProcessingService(PaymentService paymentService,
      ExternalPaymentServiceClient externalPaymentService,
      KafkaTemplate<String, Object> kafkaTemplate) {
    this.paymentService = paymentService;
    this.externalPaymentService = externalPaymentService;
    this.kafkaTemplate = kafkaTemplate;
  }

  /**
   * Processes the order creation event from Kafka
   */
  @Transactional
  public void processOrderCreatedEvent(OrderCreatedEvent event) {
    try {
      Payment payment = createPendingPayment(event);

      PaymentIdResponse paymentResponse = externalPaymentService.generatePaymentId();
      PaymentStatus paymentStatus = paymentResponse.getStatus();

      log.info("Start updating order status in payment service");
      paymentService.updatePayment(payment.getId(), paymentStatus);

      sendPaymentProcessedEvent(event.getOrderId(), payment.getId(), paymentStatus);
      log.info("Sending an event to Kafka");

    } catch (Exception e) {
      sendPaymentFailedEvent(event.getOrderId());
      log.error("Sending an event to Kafka_failed");
      throw new RuntimeException("Failed to process payment for order: " + event.getOrderId(), e);
    }
  }

  private Payment createPendingPayment(OrderCreatedEvent event) {
    Payment payment = new Payment();
    payment.setOrderId(event.getOrderId());
    payment.setUserId(event.getUserId());
    payment.setPaymentAmount(event.getAmount());
    payment.setStatus(PaymentStatus.PENDING);
    payment.setTimestamp(LocalDateTime.now());

    return paymentService.createPayment(payment);
  }

  private void sendPaymentProcessedEvent(Long orderId, String paymentId, PaymentStatus status) {
    PaymentProcessedEvent event = new PaymentProcessedEvent(orderId, paymentId, status);
    kafkaTemplate.send(ORDER_PROCESSED_TOPIC, String.valueOf(orderId), event);
  }

  private void sendPaymentFailedEvent(Long orderId) {
    PaymentProcessedEvent event = new PaymentProcessedEvent(orderId, null, FAILED);
    kafkaTemplate.send(PAYMENT_FAILED_TOPIC, String.valueOf(orderId), event);
  }
}