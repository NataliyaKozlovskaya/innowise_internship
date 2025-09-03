package com.java.core.stream;

import java.util.List;
import java.util.Set;

/**
 * Сервис для анализа бизнес-метрик заказов интернет-магазина. Предоставляет методы для сбора
 * различных статистических данных о заказах.
 */
public interface OrderAnalysis {

    /**
     * Метод получения списка уникальных городов, откуда поступали заказы Orders
     *
     * @param orders список заказов
     * @return список уникальных городов
     */
    List<String> findUniqueCities(List<Order> orders);

    /**
     * Метод получения общего дохода за все выполненные заказы
     *
     * @param orders список заказов
     * @return сумма общего дохода
     */
    double calculateTotalIncome(List<Order> orders);

    /**
     * Метод получения самого популярного продукта по продажам
     *
     * @param orders список заказов
     * @return название продукта
     */
    String findMostPopularProduct(List<Order> orders);

    /**
     * Метод вычисления среднего чека успешно доставленных заказов
     *
     * @param orders список заказов
     * @return сумма среднего чека
     */
    double calculateAverageCheck(List<Order> orders);

    /**
     * Метод нахождения клиентов, у которых больше 5 заказов
     *
     * @param orders список заказов
     * @return множество клиентов
     */
    Set<Customer> findCustomersWithMoreThan5Orders(List<Order> orders);
}
