package com.inventory.repository;

import com.inventory.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    Optional<Sale> findBySaleNumber(String saleNumber);

    List<Sale> findByClientIdOrderBySaleDateDesc(Long clientId);

    List<Sale> findBySaleDateBetweenOrderBySaleDateDesc(
            LocalDateTime from, LocalDateTime to);

    List<Sale> findByPaymentStatus(Sale.PaymentStatus status);

    @Query("SELECT SUM(s.netAmount) FROM Sale s WHERE s.saleDate BETWEEN :from AND :to")
    Double sumSaleAmountBetween(LocalDateTime from, LocalDateTime to);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.saleDate BETWEEN :from AND :to")
    Long countSalesBetween(LocalDateTime from, LocalDateTime to);
}
