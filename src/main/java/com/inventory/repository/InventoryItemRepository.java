package com.inventory.repository;

import com.inventory.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    List<InventoryItem> findByActiveTrue();

    Optional<InventoryItem> findBySku(String sku);

    List<InventoryItem> findByCategory(String category);

    @Query("SELECT i FROM InventoryItem i WHERE i.active = true AND i.quantity <= i.lowStockThreshold")
    List<InventoryItem> findLowStockItems();

    @Query("SELECT i FROM InventoryItem i WHERE i.active = true AND i.quantity = 0")
    List<InventoryItem> findOutOfStockItems();

    List<InventoryItem> findByNameContainingIgnoreCaseAndActiveTrue(String name);
}
