package com.innowise.payment.unit;

import static com.innowise.payment.enums.PaymentStatus.COMPLETED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.payment.dto.PaymentDTO;
import com.innowise.payment.entity.Payment;
import com.innowise.payment.enums.PaymentStatus;
import com.innowise.payment.exception.PaymentNotFoundException;
import com.innowise.payment.mapper.PaymentMapper;
import com.innowise.payment.repository.PaymentRepository;
import com.innowise.payment.service.impl.PaymentServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for class {@link PaymentServiceImpl}
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private PaymentMapper paymentMapper;

  @InjectMocks
  private PaymentServiceImpl paymentService;

  private final String PAYMENT_ID = "123";
  private final String USER_ID = "123";
  private final Long ORDER_ID = 123L;
  private final LocalDateTime START_DATE = LocalDateTime.of(2024, 1, 1, 0, 0);
  private final LocalDateTime END_DATE = LocalDateTime.of(2024, 1, 31, 23, 59);

  private PaymentDTO dto1;
  private PaymentDTO dto2;
  private Payment payment;
  private Payment payment1;
  private Payment payment2;

  @BeforeEach
  void setUp() {
    dto1 = new PaymentDTO(123L, "123", COMPLETED, new BigDecimal("75.25"));
    dto2 = new PaymentDTO(1234L, "123", COMPLETED, new BigDecimal("25.75"));
    payment = new Payment();
    payment1 = new Payment();
    payment2 = new Payment();
  }

  @Test
  @DisplayName("Create payment - should save and return payment")
  void createPayment_ShouldSaveAndReturnPayment() {
    Payment savedPayment = new Payment();

    when(paymentRepository.save(payment)).thenReturn(savedPayment);

    Payment result = paymentService.createPayment(payment);

    assertNotNull(result);
    assertEquals(savedPayment, result);
    verify(paymentRepository).save(payment);
  }

  @Test
  @DisplayName("Get payments by user ID - should return list of payment DTOs")
  void getPaymentsByUserId_ShouldReturnPaymentDTOs() {
    List<Payment> payments = Arrays.asList(payment1, payment2);

    when(paymentRepository.findByUserId(USER_ID)).thenReturn(payments);
    when(paymentMapper.toPaymentDTO(payment1)).thenReturn(dto1);
    when(paymentMapper.toPaymentDTO(payment2)).thenReturn(dto2);

    List<PaymentDTO> result = paymentService.getPaymentsByUserId(USER_ID);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.containsAll(Arrays.asList(dto1, dto2)));
    verify(paymentRepository).findByUserId(USER_ID);
    verify(paymentMapper, times(2)).toPaymentDTO(any(Payment.class));
  }

  @Test
  @DisplayName("Get payments by user ID - when no payments found - should return empty list")
  void getPaymentsByUserId_WhenNoPayments_ShouldReturnEmptyList() {
    when(paymentRepository.findByUserId(USER_ID)).thenReturn(List.of());

    List<PaymentDTO> result = paymentService.getPaymentsByUserId(USER_ID);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(paymentRepository).findByUserId(USER_ID);
    verify(paymentMapper, never()).toPaymentDTO(any());
  }

  @Test
  @DisplayName("Get payments by order ID - should return list of payment DTOs")
  void getPaymentsByOrderId_ShouldReturnPaymentDTOs() {
    List<Payment> payments = Arrays.asList(payment1, payment2);

    when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(payments);
    when(paymentMapper.toPaymentDTO(payment1)).thenReturn(dto1);
    when(paymentMapper.toPaymentDTO(payment2)).thenReturn(dto2);

    List<PaymentDTO> result = paymentService.getPaymentsByOrderId(ORDER_ID);

    assertNotNull(result);
    assertEquals(2, result.size());
    verify(paymentRepository).findByOrderId(ORDER_ID);
    verify(paymentMapper, times(2)).toPaymentDTO(any(Payment.class));
  }

  @Test
  @DisplayName("Get payments by statuses - should return filtered payment DTOs")
  void getPaymentsByStatuses_ShouldReturnFilteredPaymentDTOs() {
    List<PaymentStatus> statuses = Arrays.asList(COMPLETED, PaymentStatus.PENDING);
    List<Payment> payments = Arrays.asList(payment1, payment2);

    when(paymentRepository.findByStatusIn(statuses)).thenReturn(payments);
    when(paymentMapper.toPaymentDTO(payment1)).thenReturn(dto1);
    when(paymentMapper.toPaymentDTO(payment2)).thenReturn(dto2);

    List<PaymentDTO> result = paymentService.getPaymentsByStatuses(statuses);

    assertNotNull(result);
    assertEquals(2, result.size());
    verify(paymentRepository).findByStatusIn(statuses);
    verify(paymentMapper, times(2)).toPaymentDTO(any(Payment.class));
  }

  @Test
  @DisplayName("Get total sum for period - with payments found - should return correct sum")
  void getTotalSumForPeriod_WithPayments_ShouldReturnCorrectSum() {
    payment1.setPaymentAmount(new BigDecimal("100.50"));
    payment2.setPaymentAmount(new BigDecimal("200.75"));
    List<Payment> payments = Arrays.asList(payment1, payment2);

    when(paymentRepository.findCompletedPaymentsInPeriod(START_DATE, END_DATE)).thenReturn(
        payments);

    BigDecimal result = paymentService.getTotalSumForPeriod(START_DATE, END_DATE);

    assertNotNull(result);
    assertEquals(new BigDecimal("301.25"), result);
    verify(paymentRepository).findCompletedPaymentsInPeriod(START_DATE, END_DATE);
  }

  @Test
  @DisplayName("Get total sum for period - when no payments found - should return zero")
  void getTotalSumForPeriod_WhenNoPayments_ShouldReturnZero() {
    when(paymentRepository.findCompletedPaymentsInPeriod(START_DATE, END_DATE)).thenReturn(
        List.of());

    BigDecimal result = paymentService.getTotalSumForPeriod(START_DATE, END_DATE);

    assertNotNull(result);
    assertEquals(BigDecimal.ZERO, result);
    verify(paymentRepository).findCompletedPaymentsInPeriod(START_DATE, END_DATE);
  }

  @Test
  @DisplayName("Update payment - when payment exists - should update status and return DTO")
  void updatePayment_WhenPaymentExists_ShouldUpdateAndReturnDTO() {
    payment.setStatus(PaymentStatus.PENDING);
    Payment updatedPayment = new Payment();
    updatedPayment.setStatus(PaymentStatus.COMPLETED);

    PaymentDTO expectedDTO = new PaymentDTO(123L, "123", COMPLETED, new BigDecimal("75.25"));

    when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
    when(paymentRepository.save(payment)).thenReturn(updatedPayment);
    when(paymentMapper.toPaymentDTO(any())).thenReturn(expectedDTO);

    PaymentDTO result = paymentService.updatePayment(PAYMENT_ID, COMPLETED);

    assertNotNull(result);
    assertEquals(expectedDTO, result);
    assertEquals(COMPLETED, payment.getStatus());
    verify(paymentRepository).findById(PAYMENT_ID);
    verify(paymentRepository).save(payment);
    verify(paymentMapper).toPaymentDTO(any());
  }

  @Test
  @DisplayName("Update payment - when payment not found - should throw PaymentNotFoundException")
  void updatePayment_WhenPaymentNotFound_ShouldThrowException() {
    when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

    assertThrows(PaymentNotFoundException.class,
        () -> paymentService.updatePayment(PAYMENT_ID, COMPLETED));

    verify(paymentRepository).findById(PAYMENT_ID);
    verify(paymentRepository, never()).save(any());
    verify(paymentMapper, never()).toPaymentDTO(any());
  }
}