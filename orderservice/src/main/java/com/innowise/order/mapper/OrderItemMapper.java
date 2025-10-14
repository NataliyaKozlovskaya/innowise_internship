package com.innowise.order.mapper;

import com.innowise.order.dto.OrderItemDTO;
import com.innowise.order.entity.OrderItem;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting between OrderItem entity and OrderItemDTO.
 * Handles mapping of nested Item entity fields to flat DTO structure
 */
 @Mapper(componentModel = "spring")
public interface OrderItemMapper {

  @Mapping(target = "itemId", source = "item.id")
  @Mapping(target = "itemName", source = "item.name")
  @Mapping(target = "itemPrice", source = "item.price")
  OrderItemDTO toDTO(OrderItem orderItem);

  List<OrderItemDTO> toDTOList(List<OrderItem> orderItems);
}
