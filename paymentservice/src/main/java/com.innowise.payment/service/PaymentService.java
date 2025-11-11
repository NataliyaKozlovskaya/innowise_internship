package com.innowise.payment.service;

import com.innowise.payment.dto.PaymentDTO;
import com.innowise.payment.entity.Payment;
import com.innowise.payment.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for managing payment operations
 */
public interface PaymentService {

  /**
   * Creates and saves a new payment in the database
   *
   * @param payment new payment
   * @return Payment
   */
  Payment createPayment(Payment payment);

  /**
   * Finds all payments associated with the specified order ID
   *
   * @param orderId order id
   * @return list of PaymentDTO
   */
  List<PaymentDTO> getPaymentsByOrderId(Long orderId);

  /**
   * Finds all payments associated with the specified user ID
   *
   * @param userId user id
   * @return list of Payment
   */
  List<PaymentDTO> getPaymentsByUserId(String userId);

  /**
   * Finds all payments with the specified statuses
   *
   * @param statuses payment statuses
   * @return list of Payment
   */
  List<PaymentDTO> getPaymentsByStatuses(List<PaymentStatus> statuses);

  /**
   * Calculates the total sum of all payments within the specified date period
   *
   * @param startDate start date
   * @param endDate   end date
   * @return sum
   */
  BigDecimal getTotalSumForPeriod(LocalDateTime startDate, LocalDateTime endDate);

  /**
   * Update order status by payment ID
   *
   * @param id     payment id
   * @param status payment status
   * @return Payment
   */
  PaymentDTO updatePayment(String id, PaymentStatus status);
}
