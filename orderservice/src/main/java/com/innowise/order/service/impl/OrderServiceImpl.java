package com.innowise.order.service.impl;

import com.innowise.order.dto.CreateOrderRequest;
import com.innowise.order.dto.OrderDTO;
import com.innowise.order.dto.OrderItemRequest;
import com.innowise.order.entity.Item;
import com.innowise.order.entity.Order;
import com.innowise.order.enums.OrderStatus;
import com.innowise.order.exception.ItemNotFoundException;
import com.innowise.order.exception.OrderNotFoundException;
import com.innowise.order.exception.UserNotFoundException;
import com.innowise.order.exception.UserServiceUnavailableException;
import com.innowise.order.kafka.OrderEventService;
import com.innowise.order.mapper.OrderMapper;
import com.innowise.order.repository.ItemRepository;
import com.innowise.order.repository.OrderRepository;
import com.innowise.order.rest.OrderClientService;
import com.innowise.order.service.OrderService;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

  private static final String ORDER_NOT_FOUND = "Order not found with id: ";
  private final OrderRepository orderRepository;
  private final ItemRepository itemRepository;
  private final OrderMapper orderMapper;
  private final OrderClientService userClientService;
  private final OrderEventService orderEventService;

  public OrderServiceImpl(OrderRepository orderRepository, ItemRepository itemRepository,
      OrderMapper orderMapper, OrderClientService userClientService,
      OrderEventService orderEventService
  ) {
    this.orderRepository = orderRepository;
    this.itemRepository = itemRepository;
    this.orderMapper = orderMapper;
    this.userClientService = userClientService;
    this.orderEventService = orderEventService;
  }

  @Transactional
  @Override
  public OrderDTO createOrder(CreateOrderRequest request) {
    validateUserExists(request.userId());
    Map<Long, Item> availableItems = fetchAndValidateItems(request.items());
    Map<Long, Integer> itemQuantities = extractItemQuantities(request.items());

    Order order = buildOrder(request.userId(), availableItems, itemQuantities);
    Order savedOrder = orderRepository.save(order);

    // Отправляем событие в Kafka
    orderEventService.sendOrderCreatedEvent(
        savedOrder.getId().toString(),
        savedOrder.getUserId(),
        savedOrder.getAmount()// надо посчитать исходя из заказа TODO
    );

    return orderMapper.toOrderDTO(savedOrder);
  }

  @Transactional(readOnly = true)
  @Override
  public OrderDTO getOrderById(Long id) {
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND + id));
    return orderMapper.toOrderDTO(order);
  }

  @Transactional(readOnly = true)
  @Override
  public List<OrderDTO> getOrdersByUserId(String userId) {
    return orderRepository.findAllByUserId(userId)
        .stream()
        .map(orderMapper::toOrderDTO)
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public List<OrderDTO> getOrdersByIds(List<Long> ids) {
    List<Order> orders = orderRepository.findByIdIn(ids);
    return orders.stream()
        .map(orderMapper::toOrderDTO)
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public List<OrderDTO> getOrdersByStatuses(List<OrderStatus> statuses) {
    List<Order> orders = orderRepository.findByStatusIn(statuses);
    return orders.stream()
        .map(orderMapper::toOrderDTO)
        .toList();
  }

  @Transactional
  @Override
  public OrderDTO updateOrder(Long id, OrderStatus newStatus) {
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND + id));

    order.setStatus(newStatus);
    Order updatedOrder = orderRepository.save(order);
    return orderMapper.toOrderDTO(updatedOrder);
  }

  // В OrderService добавляем метод:// todo
//  public void updateOrderStatus(String orderId, String paymentStatus) {
//    Order order = orderRepository.findById(orderId)
//        .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
//
//    OrderStatus newStatus = "COMPLETED".equals(paymentStatus)
//        ? OrderStatus.PAID
//        : OrderStatus.PAYMENT_FAILED;
//
//    order.setStatus(newStatus);
//    orderRepository.save(order);
//  }

  @Override
  @Transactional
  public void deleteOrder(Long id) {
    if (!orderRepository.existsById(id)) {
      throw new OrderNotFoundException(ORDER_NOT_FOUND + id);
    }
    orderRepository.deleteById(id);
  }

  @Override
  @Transactional
  public void deleteOrderByUserId(String id) {
    orderRepository.deleteByUserId(id);
  }

  /**
   * Validates that the user exists by calling the user service
   */
  private void validateUserExists(String userId) {
    try {
      userClientService.getUserById(userId);
    } catch (UserNotFoundException ex) {
      throw new UserServiceUnavailableException("User not found with id: " + userId, ex);
    } catch (Exception ex) {
      throw new UserServiceUnavailableException("User service unavailable", ex);
    }
  }

  /**
   * Fetches items from repository and validates availability.
   *
   * @param orderItems list of requested order items
   * @return map of available items by ID
   * @throws ItemNotFoundException if any items are not found
   */
  private Map<Long, Item> fetchAndValidateItems(List<OrderItemRequest> orderItems) {
    List<Long> itemIds = extractItemIds(orderItems);
    Map<Long, Item> availableItems = fetchItemsByIds(itemIds);

    validateAllItemsAvailable(itemIds, availableItems);
    return availableItems;
  }

  /**
   * Extracts item IDs from order requests.
   */
  private List<Long> extractItemIds(List<OrderItemRequest> orderItems) {
    return orderItems.stream()
        .map(OrderItemRequest::itemId)
        .toList();
  }

  /**
   * Fetches items by IDs and returns as map.
   */
  private Map<Long, Item> fetchItemsByIds(List<Long> itemIds) {
    return itemRepository.findByIdIn(itemIds).stream()
        .filter(item -> item.getId() != null)
        .collect(Collectors.toMap(Item::getId, Function.identity()));
  }

  /**
   * Validates that all requested items are available.
   */
  private void validateAllItemsAvailable(List<Long> requestedIds, Map<Long, Item> availableItems) {
    List<Long> missingIds = requestedIds.stream()
        .filter(id -> !availableItems.containsKey(id))
        .toList();

    if (!missingIds.isEmpty()) {
      throw new ItemNotFoundException("Items not found: " + missingIds);
    }
  }

  /**
   * Extracts item quantities from order requests.
   */
  private Map<Long, Integer> extractItemQuantities(List<OrderItemRequest> orderItems) {
    return orderItems.stream()
        .collect(Collectors.toMap(OrderItemRequest::itemId, OrderItemRequest::quantity));
  }

  /**
   * Builds order entity with items and quantities.
   */
  private Order buildOrder(String userId, Map<Long, Item> availableItems,
      Map<Long, Integer> itemQuantities) {
    Order order = new Order(userId, OrderStatus.PENDING);

    availableItems.forEach((itemId, item) -> {
      Integer quantity = itemQuantities.get(itemId);
      order.addOrderItem(item, quantity);
    });

    return order;
  }
}
