package com.java.core.stream;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of a service for analyzing business metrics of online store orders
 */
public class OrderAnalysisImpl implements OrderAnalysis {

  @Override
  public List<String> findUniqueCities(List<Order> orders) {
    return orders.stream()
        .map(order -> order.getCustomer().getCity())
        .filter(city -> city != null && !city.trim().isEmpty())
        .distinct()
        .collect(Collectors.toList());
  }

  @Override
  public double calculateTotalIncome(List<Order> orders) {
    return orders.stream()
        .filter(order -> order != null && order.getStatus() == OrderStatus.DELIVERED)
        .filter(order -> order.getItems() != null)
        .flatMap(order -> order.getItems().stream())
        .filter(Objects::nonNull)
        .filter(item -> item.getPrice() >= 0
            && item.getQuantity() >= 0)
        .mapToDouble(item -> item.getQuantity() * item.getPrice())
        .sum();
  }

  @Override
  public String findMostPopularProduct(List<Order> orders) {
    return orders.stream()
        .filter(order -> order != null && order.getItems() != null)
        .flatMap(order -> order.getItems().stream())
        .filter(item -> item != null && item.getProductName() != null)
        .filter(item -> item.getQuantity() > 0)
        .collect(Collectors.groupingBy(
            OrderItem::getProductName,
            Collectors.summingInt(OrderItem::getQuantity)
        ))
        .entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse("No products found");
  }

  @Override
  public double calculateAverageCheck(List<Order> orders) {
    return orders.stream()
        .filter(Objects::nonNull)
        .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
        .filter(order -> order.getItems() != null && !order.getItems().isEmpty())
        .mapToDouble(this::calculateOrder)
        .filter(total -> total > 0)
        .average()
        .orElse(0.0);
  }

  private double calculateOrder(Order order) {
    return order.getItems().stream()
        .filter(Objects::nonNull)
        .filter(item -> item.getPrice() >= 0 && item.getQuantity() >= 0)
        .mapToDouble(item -> item.getQuantity() * item.getPrice())
        .sum();
  }

  @Override
  public Set<Customer> findCustomersWithMoreThan5Orders(List<Order> orders) {
    return orders.stream()
        .filter(order -> order != null && order.getCustomer() != null)
        .collect(Collectors.groupingBy(
            Order::getCustomer,
            Collectors.counting()
        ))
        .entrySet().stream()
        .filter(entry -> entry.getValue() > 5)
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());
  }
}
