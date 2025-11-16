package com.innowise.payment.controller;

import com.innowise.payment.dto.PaymentDTO;
import com.innowise.payment.enums.PaymentStatus;
import com.innowise.payment.service.impl.PaymentServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

  private final PaymentServiceImpl paymentService;

  public PaymentController(PaymentServiceImpl paymentService) {
    this.paymentService = paymentService;
  }

  @GetMapping("/order/{orderId}")
  public ResponseEntity<List<PaymentDTO>> getPaymentsByOrderId(@PathVariable Long orderId) {
    List<PaymentDTO> response = paymentService.getPaymentsByOrderId(orderId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<PaymentDTO>> getPaymentsByUserId(@PathVariable String userId) {
    List<PaymentDTO> response = paymentService.getPaymentsByUserId(userId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/status")
  public ResponseEntity<List<PaymentDTO>> getPaymentsByStatuses(
      @RequestParam List<PaymentStatus> statuses) {
    List<PaymentDTO> response = paymentService.getPaymentsByStatuses(statuses);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/total")
  public ResponseEntity<BigDecimal> getTotalPaymentsForPeriod(
      @RequestParam LocalDateTime start,
      @RequestParam LocalDateTime end) {

    BigDecimal total = paymentService.getTotalSumForPeriod(start, end);
    return ResponseEntity.ok(total);
  }
}
