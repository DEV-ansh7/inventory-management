package com.inventory.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardDTO {
    private long totalItems;
    private long activeItems;
    private long lowStockCount;
    private long outOfStockCount;
    private long totalSalesToday;
    private BigDecimal revenueTodayAmount;
    private BigDecimal totalPendingPayments;
    private long overduePaymentCount;
    private List<AlertDTO> alerts;

    @Data
    @Builder
    public static class AlertDTO {
        private String type;      // LOW_STOCK | OUT_OF_STOCK | PAYMENT_DUE | PAYMENT_OVERDUE
        private String title;
        private String message;
        private String severity; // warning | danger | info
    }
}
