package com.innowise.payment.service.impl;

import com.innowise.payment.dto.PaymentDTO;
import com.innowise.payment.entity.Payment;
import com.innowise.payment.enums.PaymentStatus;
import com.innowise.payment.exception.PaymentNotFoundException;
import com.innowise.payment.mapper.PaymentMapper;
import com.innowise.payment.repository.PaymentRepository;
import com.innowise.payment.service.PaymentService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

  private final PaymentRepository paymentRepository;
  private final PaymentMapper paymentMapper;


  public PaymentServiceImpl(PaymentRepository paymentRepository, PaymentMapper paymentMapper) {
    this.paymentRepository = paymentRepository;
    this.paymentMapper = paymentMapper;
  }

  @Override
  @Transactional
  public Payment createPayment(Payment payment) {
    return paymentRepository.save(payment);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PaymentDTO> getPaymentsByUserId(String userId) {
    return paymentRepository.findByUserId(userId)
        .stream()
        .map(paymentMapper::toPaymentDTO)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<PaymentDTO> getPaymentsByOrderId(Long orderId) {
    return paymentRepository.findByOrderId(orderId)
        .stream()
        .map(paymentMapper::toPaymentDTO)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<PaymentDTO> getPaymentsByStatuses(List<PaymentStatus> statuses) {
    return paymentRepository.findByStatusIn(statuses)
        .stream()
        .map(paymentMapper::toPaymentDTO)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public BigDecimal getTotalSumForPeriod(LocalDateTime startDate, LocalDateTime endDate) {
    List<Payment> payments = paymentRepository.findCompletedPaymentsInPeriod(startDate, endDate);

    if (payments.isEmpty()) {
      log.error("Payments were not found for this period: {}, {}", startDate, endDate);
      return BigDecimal.ZERO;
    }
    return payments.stream()
        .map(Payment::getPaymentAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @Override
  @Transactional
  public PaymentDTO updatePayment(String id, PaymentStatus status) {
    Payment payment = paymentRepository.findById(id)
        .orElseThrow(
            () -> new PaymentNotFoundException("Payment with id " + id + " was not found"));

    payment.setStatus(status);
    paymentRepository.save(payment);
    return paymentMapper.toPaymentDTO(payment);
  }

  public void deleteAll(){
    paymentRepository.deleteAll();
  }
}
