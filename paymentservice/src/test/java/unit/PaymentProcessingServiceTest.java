package unit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.payment.dto.PaymentDTO;
import com.innowise.payment.dto.PaymentIdResponse;
import com.innowise.payment.dto.kafka.OrderCreatedEvent;
import com.innowise.payment.dto.kafka.PaymentProcessedEvent;
import com.innowise.payment.entity.Payment;
import com.innowise.payment.enums.PaymentStatus;
import com.innowise.payment.kafka.PaymentProcessingService;
import com.innowise.payment.rest.ExternalPaymentServiceClient;
import com.innowise.payment.service.PaymentService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Tests for class {@link PaymentProcessingService}
 */
@ExtendWith(MockitoExtension.class)
class PaymentProcessingServiceTest {

  @Mock
  private PaymentService paymentService;

  @Mock
  private ExternalPaymentServiceClient externalPaymentService;

  @Mock
  private KafkaTemplate<String, Object> kafkaTemplate;

  @InjectMocks
  private PaymentProcessingService paymentProcessingService;

  private final Long ORDER_ID = 123L;
  private final String USER_ID = "123";
  private final String PAYMENT_ID = "12345";
  private final BigDecimal ORDER_AMOUNT = new BigDecimal("100.50");

  private OrderCreatedEvent orderEvent;
  private PaymentIdResponse paymentResponse;
  private Payment pendingPayment;

  @BeforeEach
  void setUp() {
    orderEvent = new OrderCreatedEvent();
    orderEvent.setOrderId(ORDER_ID);
    orderEvent.setUserId(USER_ID);
    orderEvent.setAmount(ORDER_AMOUNT);

    paymentResponse = new PaymentIdResponse("12345", PaymentStatus.COMPLETED);

    pendingPayment = new Payment();
  }

  @Test
  @DisplayName("Process order created event - should successfully process payment and send Kafka event")
  void processOrderCreatedEvent_ShouldProcessPaymentSuccessfully() {
    pendingPayment.setId(PAYMENT_ID);
    paymentResponse.setStatus(PaymentStatus.COMPLETED);

    when(paymentService.createPayment(any(Payment.class))).thenReturn(pendingPayment);
    when(externalPaymentService.generatePaymentId()).thenReturn(paymentResponse);
    when(paymentService.updatePayment(PAYMENT_ID, PaymentStatus.COMPLETED))
        .thenReturn(any());

    paymentProcessingService.processOrderCreatedEvent(orderEvent);

    verify(paymentService).createPayment(any(Payment.class));
    verify(externalPaymentService).generatePaymentId();
    verify(paymentService).updatePayment(PAYMENT_ID, PaymentStatus.COMPLETED);
    verify(kafkaTemplate).send(eq("order-payment-processed"), eq(String.valueOf(ORDER_ID)), any(
        PaymentProcessedEvent.class));
    verify(kafkaTemplate, never()).send(eq("payment-failed"), anyString(), any());
  }

  @Test
  @DisplayName("Process order created event - when external service fails - should send failed event and throw exception")
  void processOrderCreatedEvent_WhenExternalServiceFails_ShouldSendFailedEvent() {
    pendingPayment.setId(PAYMENT_ID);

    when(paymentService.createPayment(any(Payment.class))).thenReturn(pendingPayment);
    when(externalPaymentService.generatePaymentId()).thenThrow(
        new RuntimeException("External service unavailable"));

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> paymentProcessingService.processOrderCreatedEvent(orderEvent));

    assertTrue(exception.getMessage().contains("Failed to process payment for order: " + ORDER_ID));

    verify(paymentService).createPayment(any(Payment.class));
    verify(externalPaymentService).generatePaymentId();
    verify(paymentService, never()).updatePayment(anyString(), any());
    verify(kafkaTemplate).send(eq("payment-failed"), eq(String.valueOf(ORDER_ID)),
        any(PaymentProcessedEvent.class));
    verify(kafkaTemplate, never()).send(eq("order-payment-processed"), anyString(), any());
  }

  @Test
  @DisplayName("Process order created event - when payment creation fails - should send failed event and throw exception")
  void processOrderCreatedEvent_WhenPaymentCreationFails_ShouldSendFailedEvent() {
    when(paymentService.createPayment(any(Payment.class))).thenThrow(
        new RuntimeException("Database error"));

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> paymentProcessingService.processOrderCreatedEvent(orderEvent));

    assertTrue(exception.getMessage().contains("Failed to process payment for order: " + ORDER_ID));

    verify(paymentService).createPayment(any(Payment.class));
    verify(externalPaymentService, never()).generatePaymentId();
    verify(paymentService, never()).updatePayment(anyString(), any());
    verify(kafkaTemplate).send(eq("payment-failed"), eq(String.valueOf(ORDER_ID)),
        any(PaymentProcessedEvent.class));
  }

  @Test
  @DisplayName("Process order created event - when payment update fails - should send failed event and throw exception")
  void processOrderCreatedEvent_WhenPaymentUpdateFails_ShouldSendFailedEvent() {
    pendingPayment.setId(PAYMENT_ID);
    paymentResponse.setStatus(PaymentStatus.COMPLETED);

    when(paymentService.createPayment(any(Payment.class))).thenReturn(pendingPayment);
    when(externalPaymentService.generatePaymentId()).thenReturn(paymentResponse);
    when(paymentService.updatePayment(PAYMENT_ID, PaymentStatus.COMPLETED))
        .thenThrow(new RuntimeException("Update failed"));

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> paymentProcessingService.processOrderCreatedEvent(orderEvent));

    assertTrue(exception.getMessage().contains("Failed to process payment for order: " + ORDER_ID));

    verify(paymentService).createPayment(any(Payment.class));
    verify(externalPaymentService).generatePaymentId();
    verify(paymentService).updatePayment(PAYMENT_ID, PaymentStatus.COMPLETED);
    verify(kafkaTemplate).send(eq("payment-failed"), eq(String.valueOf(ORDER_ID)),
        any(PaymentProcessedEvent.class));
  }

  @Test
  @DisplayName("Process order created event - with different payment statuses - should handle correctly")
  void processOrderCreatedEvent_WithDifferentPaymentStatuses_ShouldHandleCorrectly() {
    OrderCreatedEvent orderEvent = new OrderCreatedEvent();
    orderEvent.setOrderId(ORDER_ID);
    orderEvent.setUserId(USER_ID);
    orderEvent.setAmount(ORDER_AMOUNT);

    Payment pendingPayment = new Payment();
    pendingPayment.setId(PAYMENT_ID);

    PaymentIdResponse paymentResponse = new PaymentIdResponse("123", PaymentStatus.COMPLETED);
    paymentResponse.setStatus(PaymentStatus.PENDING); // Different status

    when(paymentService.createPayment(any(Payment.class))).thenReturn(pendingPayment);
    when(externalPaymentService.generatePaymentId()).thenReturn(paymentResponse);
    when(paymentService.updatePayment(PAYMENT_ID, PaymentStatus.PENDING))
        .thenReturn(new PaymentDTO(1L, "123", PaymentStatus.COMPLETED, new BigDecimal(102.23)));

    paymentProcessingService.processOrderCreatedEvent(orderEvent);

    verify(paymentService).updatePayment(PAYMENT_ID, PaymentStatus.PENDING);
    verify(kafkaTemplate).send(eq("order-payment-processed"), eq(String.valueOf(ORDER_ID)),
        any(PaymentProcessedEvent.class));
  }
}