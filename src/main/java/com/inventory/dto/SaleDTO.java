package com.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

public class SaleDTO {

    @Data
    public static class SaleLineItem {
        @NotNull
        private Long itemId;
        @NotNull @Min(1)
        private Integer quantity;
        @NotNull @DecimalMin("0.01")
        private BigDecimal unitPrice;
    }

    @Data
    public static class CreateSaleRequest {
        private Long clientId;
        @NotEmpty
        private List<SaleLineItem> items;
        private BigDecimal discount = BigDecimal.ZERO;
        private String notes;
        // Payment details
        @NotBlank
        private String paymentMethod;     // UPI, CASH, BANK_TRANSFER, DEFERRED
        private String paymentDueDate;    // required if DEFERRED (yyyy-MM-dd)
    }

    @Data
    public static class SaleResponse {
        private Long id;
        private String saleNumber;
        private String clientName;
        private BigDecimal totalAmount;
        private BigDecimal discount;
        private BigDecimal netAmount;
        private String paymentStatus;
        private String saleDate;
        private List<SaleItemResponse> items;
    }

    @Data
    public static class SaleItemResponse {
        private Long itemId;
        private String itemName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
}
