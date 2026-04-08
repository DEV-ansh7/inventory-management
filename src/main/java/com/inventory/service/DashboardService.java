package com.inventory.service;

import com.inventory.dto.DashboardDTO;
import com.inventory.dto.DashboardDTO.AlertDTO;
import com.inventory.entity.*;
import com.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final InventoryItemRepository itemRepo;
    private final SaleRepository saleRepo;
    private final PaymentRepository paymentRepo;

    public DashboardDTO getSummary() {
        List<InventoryItem> lowStock = itemRepo.findLowStockItems();
        List<InventoryItem> outOfStock = itemRepo.findOutOfStockItems();
        List<PaymentRecord> overduePayments = paymentRepo.findOverduePayments(LocalDate.now());

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDateTime.now();

        Long salesCount = saleRepo.countSalesBetween(todayStart, todayEnd);
        Double revenueDouble = saleRepo.sumSaleAmountBetween(todayStart, todayEnd);
        Double pendingDouble = paymentRepo.sumPendingPayments();

        List<AlertDTO> alerts = buildAlerts(lowStock, outOfStock, overduePayments);

        return DashboardDTO.builder()
                .totalItems(itemRepo.count())
                .activeItems(itemRepo.findByActiveTrue().size())
                .lowStockCount(lowStock.size())
                .outOfStockCount(outOfStock.size())
                .totalSalesToday(salesCount != null ? salesCount : 0)
                .revenueTodayAmount(revenueDouble != null ? BigDecimal.valueOf(revenueDouble) : BigDecimal.ZERO)
                .totalPendingPayments(pendingDouble != null ? BigDecimal.valueOf(pendingDouble) : BigDecimal.ZERO)
                .overduePaymentCount(overduePayments.size())
                .alerts(alerts)
                .build();
    }

    private List<AlertDTO> buildAlerts(List<InventoryItem> lowStock,
                                        List<InventoryItem> outOfStock,
                                        List<PaymentRecord> overdue) {
        List<AlertDTO> alerts = new ArrayList<>();

        outOfStock.forEach(item -> alerts.add(AlertDTO.builder()
                .type("OUT_OF_STOCK")
                .title("Out of Stock")
                .message(item.getName() + " is completely out of stock!")
                .severity("danger")
                .build()));

        lowStock.stream()
                .filter(i -> i.getQuantity() > 0)
                .forEach(item -> alerts.add(AlertDTO.builder()
                        .type("LOW_STOCK")
                        .title("Low Stock")
                        .message(item.getName() + " has only " + item.getQuantity()
                                + " " + item.getUnit() + " remaining (threshold: " + item.getLowStockThreshold() + ")")
                        .severity("warning")
                        .build()));

        overdue.forEach(p -> alerts.add(AlertDTO.builder()
                .type("PAYMENT_OVERDUE")
                .title("Payment Overdue")
                .message("Payment of ₹" + p.getAmount() + " for "
                        + (p.getSale() != null ? p.getSale().getSaleNumber() : "N/A")
                        + " was due on " + p.getDueDate())
                .severity("danger")
                .build()));

        return alerts;
    }
}
