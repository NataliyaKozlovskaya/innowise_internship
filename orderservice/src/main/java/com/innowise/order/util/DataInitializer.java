package com.innowise.order.util;

import com.innowise.order.entity.Item;
import com.innowise.order.repository.ItemRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Component responsible for initializing database with sample data on application startup
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class DataInitializer implements CommandLineRunner {

  private final ItemRepository itemRepository;

  @Override
  public void run(String... args) {
    try {
      if (itemRepository.count() == 0) {
        List<Item> items = List.of(
            new Item("Laptop", new BigDecimal("999.99")),
            new Item("Smartphone", new BigDecimal("499.99")),
            new Item("Tablet", new BigDecimal("299.99")),
            new Item("Headphones", new BigDecimal("149.99")),
            new Item("Keyboard", new BigDecimal("79.99")),
            new Item("Mouse", new BigDecimal("29.99")),
            new Item("Monitor", new BigDecimal("199.99")),
            new Item("Webcam", new BigDecimal("89.99")),
            new Item("Printer", new BigDecimal("249.99")),
            new Item("Speaker", new BigDecimal("119.99"))
        );

        itemRepository.saveAll(items);
        log.info("Initial items data loaded successfully. Loaded {} items.", items.size());
      } else {
        log.info("Database already contains {} items. Skipping data initialization.",
            itemRepository.count());
      }
    } catch (Exception e) {
      log.error("Error during data initialization: {}", e.getMessage());
    }
  }
}