package com.innowise.payment.mapper;

import com.innowise.payment.dto.PaymentDTO;
import com.innowise.payment.dto.kafka.OrderCreatedEvent;
import com.innowise.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting between Payment entity and PaymentDTO
 */
@Mapper(componentModel = "spring")
public interface PaymentMapper {

  PaymentDTO toPaymentDTO(Payment payment);

  @Mapping(target = "status", constant = "PENDING")
  @Mapping(target = "paymentAmount", source = "amount")
  @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.now())")
  Payment toPendingPayment(OrderCreatedEvent event);
}
