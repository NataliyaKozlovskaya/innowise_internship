package com.innowise.payment.kafka;


import static com.innowise.payment.enums.PaymentStatus.FAILED;

import com.innowise.payment.dto.kafka.OrderCreatedEvent;
import com.innowise.payment.dto.PaymentIdResponse;
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
 * todo
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
   * Обрабатывает событие создания заказа из Kafka
   */
  @Transactional
  public void processOrderCreatedEvent(OrderCreatedEvent event) {
    try {
      // 1. We create a payment with the PENDING status
      Payment payment = createPendingPayment(event);

      // 2. Generating an ID through an external service
      PaymentIdResponse paymentResponse = externalPaymentService.generatePaymentId();

      // 3. Determine the payment status based on the ID
      PaymentStatus paymentStatus = paymentResponse.getStatus();

      // 4. Updating the payment status
      payment.setStatus(paymentStatus);
      Payment updatedPayment = paymentService.updatePayment(payment.getId(), payment);// todo
      // Do I need to go to the database twice and save twice?

      // 5. Sending an event to Kafka
      sendPaymentProcessedEvent(event.getOrderId(), payment.getId(), paymentStatus);

    } catch (Exception e) {
      sendPaymentFailedEvent(event.getOrderId());
      log.error("");
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

  private void sendPaymentProcessedEvent(String orderId, String paymentId, PaymentStatus status) {
    PaymentProcessedEvent event = new PaymentProcessedEvent(orderId, paymentId, status);
    kafkaTemplate.send(ORDER_PROCESSED_TOPIC, orderId, event);
  }

  private void sendPaymentFailedEvent(String orderId) {
    PaymentProcessedEvent event = new PaymentProcessedEvent(orderId, null, FAILED);
    kafkaTemplate.send(PAYMENT_FAILED_TOPIC, orderId, event);
  }
}