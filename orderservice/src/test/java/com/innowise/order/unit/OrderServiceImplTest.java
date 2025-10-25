package com.innowise.order.unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.innowise.order.dto.CreateOrderRequest;
import com.innowise.order.dto.OrderDTO;
import com.innowise.order.dto.OrderItemRequest;
import com.innowise.order.entity.Item;
import com.innowise.order.entity.Order;
import com.innowise.order.entity.OrderItem;
import com.innowise.order.enums.OrderStatus;
import com.innowise.order.exception.ItemNotFoundException;
import com.innowise.order.exception.OrderNotFoundException;
import com.innowise.order.exception.UserNotFoundException;
import com.innowise.order.exception.UserServiceUnavailableException;
import com.innowise.order.mapper.OrderMapper;
import com.innowise.order.repository.ItemRepository;
import com.innowise.order.repository.OrderRepository;
import com.innowise.order.rest.OrderClientService;
import com.innowise.order.service.impl.OrderServiceImpl;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private ItemRepository itemRepository;

  @Mock
  private OrderMapper orderMapper;

  @Mock
  private OrderClientService userClientService;

  @InjectMocks
  private OrderServiceImpl orderService;

  private String userId;
  private Item item1;
  private Item item2;
  private Order order;
  private OrderDTO orderDTO;

  @BeforeEach
  void setUp() {
    userId = "user-123";

    item1 = new Item("Laptop", new BigDecimal("999.99"));
    item1.setId(1L);

    item2 = new Item("Mouse", new BigDecimal("29.99"));
    item2.setId(2L);

    order = new Order(userId, OrderStatus.PENDING);
    order.setId(1L);
    order.setCreationDate(LocalDateTime.now());

    orderDTO = new OrderDTO("1", OrderStatus.PENDING, LocalDateTime.now(), List.of());
  }

  @Test
  @DisplayName("Should create order successfully when all validations pass")
  void createOrder_ShouldCreateOrder_WhenValidRequest() {
    CreateOrderRequest request = new CreateOrderRequest(
        userId,
        List.of(
            new OrderItemRequest(1L, 2),
            new OrderItemRequest(2L, 1)
        )
    );

    doNothing().when(userClientService).getUserById(userId);
    when(itemRepository.findByIdIn(List.of(1L, 2L))).thenReturn(List.of(item1, item2));
    when(orderRepository.save(any(Order.class))).thenReturn(order);
    when(orderMapper.toOrderDTO(order)).thenReturn(orderDTO);

    OrderDTO result = orderService.createOrder(request);

    assertNotNull(result);
    verify(userClientService).getUserById(userId);
    verify(itemRepository).findByIdIn(List.of(1L, 2L));
    verify(orderRepository).save(any(Order.class));
    verify(orderMapper).toOrderDTO(order);
  }

  @Test
  @DisplayName("Should throw UserNotFoundException when user does not exist")
  void createOrder_ShouldThrowUserNotFoundException_WhenUserNotFound() {
    CreateOrderRequest request = new CreateOrderRequest(userId, List.of());

    doThrow(new UserNotFoundException("User not found"))
        .when(userClientService).getUserById(userId);

    assertThrows(UserServiceUnavailableException.class, () -> orderService.createOrder(request));
    verify(userClientService).getUserById(userId);
    verifyNoInteractions(itemRepository, orderRepository);
  }

  @Test
  @DisplayName("Should throw UserServiceUnavailableException when user service fails")
  void createOrder_ShouldThrowUserServiceUnavailableException_WhenUserServiceFails() {
    CreateOrderRequest request = new CreateOrderRequest(userId, List.of());

    doThrow(new UserNotFoundException("Service unavailable"))
        .when(userClientService).getUserById(userId);

    assertThrows(UserServiceUnavailableException.class, () -> orderService.createOrder(request));
  }

  @Test
  @DisplayName("Should throw ItemNotFoundException when requested items are not found")
  void createOrder_ShouldThrowItemNotFoundException_WhenItemsNotFound() {
    CreateOrderRequest request = new CreateOrderRequest(
        userId,
        List.of(new OrderItemRequest(99L, 1))
    );

    doNothing().when(userClientService).getUserById(userId);
    when(itemRepository.findByIdIn(List.of(99L))).thenReturn(List.of());

    assertThrows(ItemNotFoundException.class, () -> orderService.createOrder(request));
    verify(itemRepository).findByIdIn(List.of(99L));
  }

  @Test
  @DisplayName("Should return order when order exists with given ID")
  void getOrderById_ShouldReturnOrder_WhenOrderExists() {
    Long orderId = 1L;
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
    when(orderMapper.toOrderDTO(order)).thenReturn(orderDTO);

    OrderDTO result = orderService.getOrderById(orderId);

    assertNotNull(result);
    verify(orderRepository).findById(orderId);
    verify(orderMapper).toOrderDTO(order);
  }

  @Test
  @DisplayName("Should throw OrderNotFoundException when order does not exist")
  void getOrderById_ShouldThrowOrderNotFoundException_WhenOrderNotExists() {
    Long orderId = 99L;
    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

    assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(orderId));
    verify(orderRepository).findById(orderId);
    verifyNoInteractions(orderMapper);
  }

  @Test
  @DisplayName("Should return orders list when orders exist with given IDs")
  void getOrdersByIds_ShouldReturnOrders_WhenOrdersExist() {
    List<Long> orderIds = List.of(1L, 2L);
    List<Order> orders = List.of(order, order);

    when(orderRepository.findByIdIn(orderIds)).thenReturn(orders);
    when(orderMapper.toOrderDTO(any(Order.class))).thenReturn(orderDTO);

    List<OrderDTO> result = orderService.getOrdersByIds(orderIds);

    assertEquals(2, result.size());
    verify(orderRepository).findByIdIn(orderIds);
    verify(orderMapper, times(2)).toOrderDTO(any(Order.class));
  }

  @Test
  @DisplayName("Should return empty list when no orders found with given IDs")
  void getOrdersByIds_ShouldReturnEmptyList_WhenNoOrdersFound() {
    List<Long> orderIds = List.of(1L, 2L);
    when(orderRepository.findByIdIn(orderIds)).thenReturn(List.of());

    List<OrderDTO> result = orderService.getOrdersByIds(orderIds);

    assertTrue(result.isEmpty());
    verify(orderRepository).findByIdIn(orderIds);
    verifyNoInteractions(orderMapper);
  }

  @Test
  @DisplayName("Should return orders when orders exist with given statuses")
  void getOrdersByStatuses_ShouldReturnOrders_WhenStatusesMatch() {
    List<OrderStatus> statuses = List.of(OrderStatus.PENDING, OrderStatus.PROCESSING);
    List<Order> orders = List.of(order, order);

    when(orderRepository.findByStatusIn(statuses)).thenReturn(orders);
    when(orderMapper.toOrderDTO(any(Order.class))).thenReturn(orderDTO);

    List<OrderDTO> result = orderService.getOrdersByStatuses(statuses);

    assertEquals(2, result.size());
    verify(orderRepository).findByStatusIn(statuses);
    verify(orderMapper, times(2)).toOrderDTO(any(Order.class));
  }

  @Test
  @DisplayName("Should update order status when order exists")
  void updateOrder_ShouldUpdateOrderStatus_WhenOrderExists() {
    Long orderId = 1L;
    OrderStatus newStatus = OrderStatus.COMPLETED;
    Order updatedOrder = new Order(userId, newStatus);
    updatedOrder.setId(orderId);

    OrderDTO updatedOrderDTO = new OrderDTO(userId, newStatus,
        LocalDateTime.now(), List.of());

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
    when(orderRepository.save(order)).thenReturn(updatedOrder);
    when(orderMapper.toOrderDTO(updatedOrder)).thenReturn(updatedOrderDTO);

    OrderDTO result = orderService.updateOrder(orderId, newStatus);

    assertNotNull(result);
    assertEquals(newStatus, result.status());
    verify(orderRepository).findById(orderId);
    verify(orderRepository).save(order);
    verify(orderMapper).toOrderDTO(updatedOrder);
  }

  @Test
  @DisplayName("Should throw OrderNotFoundException when updating non-existent order")
  void updateOrder_ShouldThrowOrderNotFoundException_WhenOrderNotExists() {
    Long orderId = 99L;
    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

    assertThrows(OrderNotFoundException.class,
        () -> orderService.updateOrder(orderId, OrderStatus.COMPLETED));
    verify(orderRepository).findById(orderId);
    verify(orderRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should delete order when order exists")
  void deleteOrder_ShouldDeleteOrder_WhenOrderExists() {
    Long orderId = 1L;
    when(orderRepository.existsById(orderId)).thenReturn(true);

    orderService.deleteOrder(orderId);

    verify(orderRepository).existsById(orderId);
    verify(orderRepository).deleteById(orderId);
  }

  @Test
  @DisplayName("Should throw OrderNotFoundException when deleting non-existent order")
  void deleteOrder_ShouldThrowOrderNotFoundException_WhenOrderNotExists() {
    Long orderId = 99L;
    when(orderRepository.existsById(orderId)).thenReturn(false);

    assertThrows(OrderNotFoundException.class, () -> orderService.deleteOrder(orderId));
    verify(orderRepository).existsById(orderId);
    verify(orderRepository, never()).deleteById(orderId);
  }

  @Test
  @DisplayName("Should return items map when all items exist in repository")
  void fetchAndValidateItems_ShouldReturnItems_WhenAllItemsExist() throws Exception {
    List<OrderItemRequest> orderItems = List.of(
        new OrderItemRequest(1L, 2),
        new OrderItemRequest(2L, 1)
    );

    when(itemRepository.findByIdIn(List.of(1L, 2L))).thenReturn(List.of(item1, item2));

    Method method = OrderServiceImpl.class.getDeclaredMethod("fetchAndValidateItems", List.class);
    method.setAccessible(true);

    @SuppressWarnings("unchecked")
    Map<Long, Item> result = (Map<Long, Item>) method.invoke(orderService, orderItems);

    assertEquals(2, result.size());
    assertTrue(result.containsKey(1L));
    assertTrue(result.containsKey(2L));
    assertEquals(item1, result.get(1L));
    assertEquals(item2, result.get(2L));
  }

  @Test
  @DisplayName("Should throw ItemNotFoundException when items are missing from repository")
  void fetchAndValidateItems_ShouldThrowItemNotFoundException_WhenItemsMissing() throws Exception {
    List<OrderItemRequest> orderItems = List.of(new OrderItemRequest(99L, 1));
    when(itemRepository.findByIdIn(List.of(99L))).thenReturn(List.of());

    Method method = OrderServiceImpl.class.getDeclaredMethod("fetchAndValidateItems", List.class);
    method.setAccessible(true);

    InvocationTargetException exception = assertThrows(InvocationTargetException.class,
        () -> method.invoke(orderService, orderItems));

    assertTrue(exception.getCause() instanceof ItemNotFoundException);
    assertEquals("Items not found: [99]", exception.getCause().getMessage());
  }

  @Test
  @DisplayName("Should extract item IDs from order item requests")
  void extractItemIds_ShouldReturnItemIds() throws Exception {
    List<OrderItemRequest> orderItems = List.of(
        new OrderItemRequest(1L, 2),
        new OrderItemRequest(2L, 1)
    );

    Method method = OrderServiceImpl.class.getDeclaredMethod("extractItemIds", List.class);
    method.setAccessible(true);

    @SuppressWarnings("unchecked")
    List<Long> result = (List<Long>) method.invoke(orderService, orderItems);

    assertEquals(List.of(1L, 2L), result);
  }

  @Test
  @DisplayName("Should extract item quantities map from order item requests")
  void extractItemQuantities_ShouldReturnQuantityMap() throws Exception {
    List<OrderItemRequest> orderItems = List.of(
        new OrderItemRequest(1L, 2),
        new OrderItemRequest(2L, 1)
    );

    Method method = OrderServiceImpl.class.getDeclaredMethod("extractItemQuantities", List.class);
    method.setAccessible(true);

    @SuppressWarnings("unchecked")
    Map<Long, Integer> result = (Map<Long, Integer>) method.invoke(orderService, orderItems);

    assertEquals(2, result.size());
    assertEquals(2, result.get(1L));
    assertEquals(1, result.get(2L));
  }

  @Test
  @DisplayName("Should validate user exists successfully")
  void validateUserExists_ShouldNotThrow_WhenUserExists() throws Exception {
    doNothing().when(userClientService).getUserById(userId);

    Method method = OrderServiceImpl.class.getDeclaredMethod("validateUserExists", String.class);
    method.setAccessible(true);

    assertDoesNotThrow(() -> method.invoke(orderService, userId));
    verify(userClientService).getUserById(userId);
  }

  @Test
  @DisplayName("Should throw UserNotFoundException when user does not exist")
  void validateUserExists_ShouldThrowUserNotFoundException_WhenUserNotFound() throws Exception {
    doThrow(new UserNotFoundException("User not found"))
        .when(userClientService).getUserById(userId);

    Method method = OrderServiceImpl.class.getDeclaredMethod("validateUserExists", String.class);
    method.setAccessible(true);

    InvocationTargetException exception = assertThrows(InvocationTargetException.class,
        () -> method.invoke(orderService, userId));

    assertTrue(exception.getCause() instanceof UserServiceUnavailableException);
    assertEquals("User not found with id: " + userId, exception.getCause().getMessage());
  }

  @Test
  @DisplayName("Should build order with items and quantities")
  void buildOrder_ShouldCreateOrderWithItems() throws Exception {
    Map<Long, Item> availableItems = Map.of(
        1L, item1,
        2L, item2
    );
    Map<Long, Integer> itemQuantities = Map.of(
        1L, 2,
        2L, 1
    );

    Method method = OrderServiceImpl.class.getDeclaredMethod("buildOrder",
        String.class, Map.class, Map.class);
    method.setAccessible(true);

    Order result = (Order) method.invoke(orderService, userId, availableItems, itemQuantities);

    assertNotNull(result);
    assertEquals(userId, result.getUserId());
    assertEquals(OrderStatus.PENDING, result.getStatus());
    assertEquals(2, result.getOrderItems().size());

    OrderItem orderItem1 = result.getOrderItems().get(0);
    OrderItem orderItem2 = result.getOrderItems().get(1);

    assertNotNull(orderItem1.getItem());
    assertNotNull(orderItem2.getItem());
  }
}