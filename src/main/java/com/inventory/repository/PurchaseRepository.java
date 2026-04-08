package com.inventory.repository;

import com.inventory.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findByItemIdOrderByPurchaseDateDesc(Long itemId);

    List<Purchase> findByPurchaseDateBetweenOrderByPurchaseDateDesc(
            LocalDateTime from, LocalDateTime to);

    @Query("SELECT SUM(p.quantity * p.unitPrice) FROM Purchase p " +
           "WHERE p.purchaseDate BETWEEN :from AND :to")
    Double sumPurchaseAmountBetween(LocalDateTime from, LocalDateTime to);
}
