package com.inventory.repository;

import com.inventory.entity.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {

    Page<TransactionLog> findAllByOrderByTransactionDateDesc(Pageable pageable);

    List<TransactionLog> findByItemIdOrderByTransactionDateDesc(Long itemId);

    List<TransactionLog> findByTransactionTypeOrderByTransactionDateDesc(
            TransactionLog.TransactionType type);

    List<TransactionLog> findByTransactionDateBetweenOrderByTransactionDateDesc(
            LocalDateTime from, LocalDateTime to);
}
