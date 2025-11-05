package com.innowise.payment.service;

import com.innowise.payment.enums.PaymentStatus;
import com.innowise.payment.entity.Payment;
import com.innowise.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
//@Transactional
public class PaymentServiceImpl implements PaymentService {

  private final PaymentRepository paymentRepository;


  public PaymentServiceImpl(PaymentRepository paymentRepository) {
    this.paymentRepository = paymentRepository;
  }


  // CRUD операции
  public Payment createPayment(Payment payment) {
    if (payment.getTimestamp() == null) {
      payment.setTimestamp(LocalDateTime.now());
    }
    return paymentRepository.save(payment);
  }

  // Специфичные операции
  public List<Payment> getPaymentsByOrderId(String orderId) {
    return paymentRepository.findByOrderId(orderId);
  }

  public List<Payment> getPaymentsByUserId(String userId) {
    return paymentRepository.findByUserId(userId);
  }

  public List<Payment> getPaymentsByStatus(PaymentStatus status) {
    return paymentRepository.findByStatus(status);
  }


  // Get total sum of payments for date period
  public BigDecimal getTotalSumForPeriod(LocalDateTime startDate, LocalDateTime endDate) {
    List<Payment> payments = paymentRepository.findCompletedPaymentsInPeriod(startDate, endDate);

    return payments.stream()
        .map(Payment::getPaymentAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
