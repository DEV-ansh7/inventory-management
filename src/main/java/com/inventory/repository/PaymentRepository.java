package com.inventory.repository;

import com.inventory.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<PaymentRecord, Long> {

    List<PaymentRecord> findBySaleId(Long saleId);

    List<PaymentRecord> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<PaymentRecord> findByPaymentStatus(PaymentRecord.PaymentStatus status);

    // Payments due within N days and not yet alerted
    @Query("SELECT p FROM PaymentRecord p WHERE p.paymentStatus = 'PENDING' " +
           "AND p.dueDate <= :alertDate AND p.alertSent = false")
    List<PaymentRecord> findDuePaymentsForAlert(@Param("alertDate") LocalDate alertDate);

    // Overdue payments (past due date, still pending)
    @Query("SELECT p FROM PaymentRecord p WHERE p.paymentStatus = 'PENDING' " +
           "AND p.dueDate < :today")
    List<PaymentRecord> findOverduePayments(@Param("today") LocalDate today);

    @Query("SELECT SUM(p.amount) FROM PaymentRecord p WHERE p.paymentStatus = 'PENDING'")
    Double sumPendingPayments();
}
