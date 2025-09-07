package com.java.core.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OrderAnalysisImplTest {

  private OrderAnalysisImpl orderAnalysis;

  @BeforeEach
  void setUp() {
    orderAnalysis = new OrderAnalysisImpl();
  }

  private Customer createCustomer(String id, String name, String city) {
    Customer customer = new Customer();
    customer.setCustomerId(id);
    customer.setName(name);
    customer.setCity(city);
    customer.setEmail(name.toLowerCase() + "@example.com");
    customer.setRegisteredAt(LocalDateTime.now());
    customer.setAge(30);
    return customer;
  }

  private OrderItem createOrderItem(String productName, int quantity, double price,
      Category category) {
    OrderItem item = new OrderItem();
    item.setProductName(productName);
    item.setQuantity(quantity);
    item.setPrice(price);
    item.setCategory(category);
    return item;
  }

  private Order createOrder(String orderId, Customer customer, OrderStatus status,
      OrderItem... items) {
    Order order = new Order();
    order.setOrderId(orderId);
    order.setOrderDate(LocalDateTime.now());
    order.setCustomer(customer);
    order.setStatus(status);
    order.setItems(List.of(items));
    return order;
  }

  @Nested
  @DisplayName("Tests of the findUniqueCities method")
  class FindUniqueCitiesTests {

    @Test
    @DisplayName("Should return an empty list for an empty list of orders")
    void shouldReturnEmptyListForEmptyOrders() {
      List<String> result = orderAnalysis.findUniqueCities(List.of());
      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return unique cities")
    void shouldReturnUniqueCities() {
      Customer customer1 = createCustomer("1", "John", "Moscow");
      Customer customer2 = createCustomer("2", "Alice", "Saint Petersburg");
      Customer customer3 = createCustomer("3", "Bob", "Moscow"); // Дубликат

      Order order1 = createOrder("1", customer1, OrderStatus.DELIVERED,
          createOrderItem("Laptop", 1, 1000, Category.ELECTRONICS));
      Order order2 = createOrder("2", customer2, OrderStatus.DELIVERED,
          createOrderItem("Book", 2, 50, Category.BOOKS));
      Order order3 = createOrder("3", customer3, OrderStatus.DELIVERED,
          createOrderItem("Phone", 1, 500, Category.ELECTRONICS));

      List<String> result = orderAnalysis.findUniqueCities(List.of(order1, order2, order3));

      assertEquals(2, result.size());
      assertTrue(result.contains("Moscow"));
      assertTrue(result.contains("Saint Petersburg"));
    }

    @Test
    @DisplayName("Should filter null and empty cities")
    void shouldFilterNullAndEmptyCities() {
      Customer customer1 = createCustomer("1", "John", null);
      Customer customer2 = createCustomer("2", "Alice", "");
      Customer customer3 = createCustomer("3", "Bob", "   ");
      Customer customer4 = createCustomer("4", "Charlie", "Moscow");

      Order order1 = createOrder("1", customer1, OrderStatus.DELIVERED,
          createOrderItem("Laptop", 1, 1000, Category.ELECTRONICS));
      Order order2 = createOrder("2", customer2, OrderStatus.DELIVERED,
          createOrderItem("Book", 2, 50, Category.BOOKS));
      Order order3 = createOrder("3", customer3, OrderStatus.DELIVERED,
          createOrderItem("Phone", 1, 500, Category.ELECTRONICS));
      Order order4 = createOrder("4", customer4, OrderStatus.DELIVERED,
          createOrderItem("Tablet", 1, 300, Category.ELECTRONICS));

      List<String> result = orderAnalysis.findUniqueCities(
          List.of(order1, order2, order3, order4));

      assertEquals(1, result.size());
      assertEquals("Moscow", result.get(0));
    }
  }

  @Nested
  @DisplayName("Tests of the calculateTotalIncome method")
  class CalculateTotalIncomeTests {

    @Test
    @DisplayName("Should return 0 for an empty order list")
    void shouldReturnZeroForEmptyOrders() {
      double result = orderAnalysis.calculateTotalIncome(List.of());
      assertEquals(0.0, result, 0.001);
    }

    @Test
    @DisplayName("Must only count delivered orders")
    void shouldConsiderOnlyDeliveredOrders() {
      Customer customer = createCustomer("1", "John", "Moscow");

      Order deliveredOrder = createOrder("1", customer, OrderStatus.DELIVERED,
          createOrderItem("Laptop", 1, 1000, Category.ELECTRONICS));
      Order processingOrder = createOrder("2", customer, OrderStatus.PROCESSING,
          createOrderItem("Book", 5, 100, Category.BOOKS));
      Order cancelledOrder = createOrder("3", customer, OrderStatus.CANCELLED,
          createOrderItem("Phone", 2, 500, Category.ELECTRONICS));

      double result = orderAnalysis.calculateTotalIncome(
          List.of(deliveredOrder, processingOrder, cancelledOrder));

      assertEquals(1000.0, result, 0.001);
    }

    @Test
    @DisplayName("Must calculate total income correctly")
    void shouldCalculateTotalIncomeCorrectly() {
      Customer customer = createCustomer("1", "John", "Moscow");

      Order order1 = createOrder("1", customer, OrderStatus.DELIVERED,
          createOrderItem("Laptop", 1, 1000, Category.ELECTRONICS),
          createOrderItem("Mouse", 2, 50, Category.ELECTRONICS));

      Order order2 = createOrder("2", customer, OrderStatus.DELIVERED,
          createOrderItem("Book", 3, 30, Category.BOOKS),
          createOrderItem("Pen", 5, 10, Category.BOOKS));

      double result = orderAnalysis.calculateTotalIncome(List.of(order1, order2));

      // (1000*1 + 50*2) + (30*3 + 10*5) = 1100 + 140 = 1240
      assertEquals(1240.0, result, 0.001);
    }
  }

  @Nested
  @DisplayName("Tests of the findMostPopularProduct method")
  class FindMostPopularProductTests {

    @Test
    @DisplayName("Should return a message for an empty order list")
    void shouldReturnMessageForEmptyOrders() {
      String result = orderAnalysis.findMostPopularProduct(List.of());
      assertEquals("No products found", result);
    }

    @Test
    @DisplayName("Must find the most popular product")
    void shouldFindMostPopularProduct() {
      Customer customer = createCustomer("1", "John", "Moscow");

      Order order1 = createOrder("1", customer, OrderStatus.DELIVERED,
          createOrderItem("Laptop", 3, 1000, Category.ELECTRONICS),
          createOrderItem("Mouse", 5, 50, Category.ELECTRONICS));

      Order order2 = createOrder("2", customer, OrderStatus.DELIVERED,
          createOrderItem("Laptop", 2, 1000, Category.ELECTRONICS),
          createOrderItem("Book", 10, 30, Category.BOOKS));

      String result = orderAnalysis.findMostPopularProduct(List.of(order1, order2));

      // Laptop: 3 + 2 = 5, Mouse: 5, Book: 10 → Book is most popular
      assertEquals("Book", result);
    }

    @Test
    @DisplayName("Should filter items with zero quantity")
    void shouldFilterZeroQuantityProducts() {
      Customer customer = createCustomer("1", "John", "Moscow");

      Order order = createOrder("1", customer, OrderStatus.DELIVERED,
          createOrderItem("Laptop", 0, 1000, Category.ELECTRONICS),
          createOrderItem("Mouse", 5, 50, Category.ELECTRONICS));

      String result = orderAnalysis.findMostPopularProduct(List.of(order));

      assertEquals("Mouse", result);
    }
  }

  @Nested
  @DisplayName("Tests of the calculateAverageCheck method")
  class CalculateAverageCheckTests {

    @Test
    @DisplayName("Should return 0 for an empty order list")
    void shouldReturnZeroForEmptyOrders() {
      double result = orderAnalysis.calculateAverageCheck(List.of());
      assertEquals(0.0, result, 0.001);
    }

    @Test
    @DisplayName("Should calculate average check only for delivered orders")
    void shouldCalculateAverageOnlyForDeliveredOrders() {
      Customer customer = createCustomer("1", "John", "Moscow");

      Order deliveredOrder1 = createOrder("1", customer, OrderStatus.DELIVERED,
          createOrderItem("Laptop", 1, 1000, Category.ELECTRONICS));

      Order deliveredOrder2 = createOrder("2", customer, OrderStatus.DELIVERED,
          createOrderItem("Book", 2, 50, Category.BOOKS));

      Order processingOrder = createOrder("3", customer, OrderStatus.PROCESSING,
          createOrderItem("Phone", 1, 500, Category.ELECTRONICS));

      double result = orderAnalysis.calculateAverageCheck(
          List.of(deliveredOrder1, deliveredOrder2, processingOrder));

      // (1000 + 100) / 2 = 550
      assertEquals(550.0, result, 0.001);
    }

    @Test
    @DisplayName("Should correctly calculate the average bill")
    void shouldCalculateAverageCheckCorrectly() {
      Customer customer = createCustomer("1", "John", "Moscow");

      Order order1 = createOrder("1", customer, OrderStatus.DELIVERED,
          createOrderItem("Laptop", 1, 1000, Category.ELECTRONICS));

      Order order2 = createOrder("2", customer, OrderStatus.DELIVERED,
          createOrderItem("Book", 2, 50, Category.BOOKS));

      Order order3 = createOrder("3", customer, OrderStatus.DELIVERED,
          createOrderItem("Phone", 1, 300, Category.ELECTRONICS));

      double result = orderAnalysis.calculateAverageCheck(List.of(order1, order2, order3));

      // (1000 + 100 + 300) / 3 = 466.666...
      assertEquals(466.666, result, 0.001);
    }
  }

  @Nested
  @DisplayName("Tests of the findCustomersWithMoreThan5Orders method")
  class FindCustomersWithMoreThan5OrdersTests {

    @Test
    @DisplayName("Should return an empty set for an empty list of orders")
    void shouldReturnEmptySetForEmptyOrders() {
      Set<Customer> result = orderAnalysis.findCustomersWithMoreThan5Orders(List.of());
      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Must find clients with more than 5 orders")
    void shouldFindCustomersWithMoreThan5Orders() {
      Customer vipCustomer = createCustomer("1", "John", "Moscow");
      Customer regularCustomer = createCustomer("2", "Alice", "Saint Petersburg");

      // We create 6 orders for a VIP client
      List<Order> vipOrders = new ArrayList<>();
      for (int i = 0; i < 6; i++) {
        vipOrders.add(createOrder(String.valueOf(i), vipCustomer, OrderStatus.DELIVERED,
            createOrderItem("Product", 1, 100, Category.ELECTRONICS)));
      }

      // We create 3 orders for a regular client
      List<Order> regularOrders = new ArrayList<>();
      for (int i = 0; i < 3; i++) {
        regularOrders.add(
            createOrder(String.valueOf(i + 10), regularCustomer, OrderStatus.DELIVERED,
                createOrderItem("Product", 1, 100, Category.ELECTRONICS)));
      }

      List<Order> allOrders = new ArrayList<>();
      allOrders.addAll(vipOrders);
      allOrders.addAll(regularOrders);

      Set<Customer> result = orderAnalysis.findCustomersWithMoreThan5Orders(allOrders);

      assertEquals(1, result.size());
      assertTrue(result.contains(vipCustomer));
      assertFalse(result.contains(regularCustomer));
    }
  }
}