package com.inventory.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PaymentDTO {

    @Data
    public static class PaymentResponse {
        private Long id;
        private Long saleId;
        private String saleNumber;
        private String clientName;
        private String clientEmail;
        private String clientPhone;
        private BigDecimal amount;
        private String paymentMethod;
        private String paymentStatus;
        private LocalDate dueDate;
        private LocalDateTime paidAt;
        private String upiTransactionId;
        private String razorpayOrderId;
        private LocalDateTime createdAt;
    }

    @Data
    public static class MarkPaidRequest {
        private String upiTransactionId;
        private String paymentMethod;
    }

    @Data
    public static class CreateUpiOrderResponse {
        private String orderId;
        private String amount;
        private String currency;
        private String key;
        private String name;
        private String description;
    }
}
