package com.inventory.scheduler;

import com.inventory.entity.*;
import com.inventory.repository.*;
import com.inventory.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertScheduler {

    private final PaymentRepository paymentRepo;
    private final InventoryItemRepository itemRepo;
    private final NotificationService notificationService;

    @Value("${app.payment.alert-days:3}")
    private int alertDays;

    /**
     * Runs every day at 9:00 AM — check for payments due within N days
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void checkDuePayments() {
        LocalDate alertDate = LocalDate.now().plusDays(alertDays);

        List<PaymentRecord> duePayments = paymentRepo.findDuePaymentsForAlert(alertDate);
        log.info("Found {} payments due for alert", duePayments.size());

        for (PaymentRecord payment : duePayments) {
            Client client = payment.getClient();
            String saleNumber = payment.getSale() != null ? payment.getSale().getSaleNumber() : "N/A";
            String clientEmail = client != null ? client.getEmail() : null;
            String clientName = client != null ? client.getName() : "Customer";

            notificationService.sendPaymentDueAlert(
                    clientEmail, clientName, saleNumber,
                    payment.getAmount().toPlainString(),
                    payment.getDueDate().toString());

            payment.setAlertSent(true);
            paymentRepo.save(payment);
        }
    }

    /**
     * Runs every day at 9:15 AM — mark overdue and send overdue alerts
     */
    @Scheduled(cron = "0 15 9 * * *")
    @Transactional
    public void checkOverduePayments() {
        List<PaymentRecord> overduePayments = paymentRepo.findOverduePayments(LocalDate.now());

        for (PaymentRecord payment : overduePayments) {
            payment.setPaymentStatus(PaymentRecord.PaymentStatus.OVERDUE);
            paymentRepo.save(payment);

            Client client = payment.getClient();
            String saleNumber = payment.getSale() != null ? payment.getSale().getSaleNumber() : "N/A";
            String clientEmail = client != null ? client.getEmail() : null;
            String clientName = client != null ? client.getName() : "Customer";

            notificationService.sendPaymentOverdueAlert(
                    clientEmail, clientName, saleNumber,
                    payment.getAmount().toPlainString(),
                    payment.getDueDate().toString());
        }
        log.info("Marked {} payments as OVERDUE", overduePayments.size());
    }

    /**
     * Runs every day at 8:00 AM — check and alert for low stock items
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void checkLowStock() {
        List<InventoryItem> lowStockItems = itemRepo.findLowStockItems();
        log.info("Found {} low-stock items", lowStockItems.size());

        lowStockItems.forEach(item ->
                notificationService.sendLowStockAlert(
                        item.getName(), item.getQuantity(), item.getLowStockThreshold()));
    }
}
