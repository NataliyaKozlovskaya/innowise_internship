package com.innowise.payment.mapper;

import com.innowise.payment.dto.PaymentDTO;
import com.innowise.payment.entity.Payment;
import org.mapstruct.Mapper;

/**
 * Mapper for converting between Payment entity and PaymentDTO
 */
@Mapper(componentModel = "spring")
public interface PaymentMapper {

  PaymentDTO toPaymentDTO(Payment payment);
}
