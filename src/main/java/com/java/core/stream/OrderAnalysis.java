package com.java.core.stream;

import java.util.List;
import java.util.Set;

/**
 * Service for analyzing business metrics of online store orders. Provides methods for collecting
 * various statistical data on orders.
 */
public interface OrderAnalysis {

  /**
   * Method for getting a list of unique cities where orders were received Orders
   *
   * @param orders list of orders
   * @return list of unique cities
   */
  List<String> findUniqueCities(List<Order> orders);

  /**
   * Method for getting total income for all completed orders
   *
   * @param orders list of orders
   * @return total income amount
   */
  double calculateTotalIncome(List<Order> orders);

  /**
   * Method to get the most popular product by sales
   *
   * @param orders list of orders
   * @return product name
   */
  String findMostPopularProduct(List<Order> orders);

  /**
   * Method for calculating the average check of successfully delivered orders
   *
   * @param orders list of orders
   * @return average check amount
   */
  double calculateAverageCheck(List<Order> orders);

  /**
   * Method for finding clients with more than 5 orders
   *
   * @param orders list of orders
   * @return set of clients
   */
  Set<Customer> findCustomersWithMoreThan5Orders(List<Order> orders);
}
