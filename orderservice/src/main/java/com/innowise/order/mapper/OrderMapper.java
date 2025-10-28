package com.innowise.order.mapper;

import com.innowise.order.dto.OrderDTO;
import com.innowise.order.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting between Order entity and OrderDTO. Uses OrderItemMapper for mapping nested
 * OrderItem collections
 */
@Mapper(componentModel = "spring", uses = OrderItemMapper.class)
public interface OrderMapper {

  @Mapping(target = "orderItems", source = "orderItems")
  OrderDTO toOrderDTO(Order order);
}
