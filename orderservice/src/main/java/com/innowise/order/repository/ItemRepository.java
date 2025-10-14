package com.innowise.order.repository;

import com.innowise.order.entity.Item;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link Item} entity operations
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
  List<Item> findByIdIn(List<Long> ids);
}