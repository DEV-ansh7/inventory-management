package com.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

public class InventoryDTO {

    @Data
    public static class CreateItemRequest {
        @NotBlank
        private String name;
        private String description;
        private String category;
        private String unit = "pcs";
        @Min(0)
        private Integer quantity = 0;
        @Min(1)
        private Integer lowStockThreshold = 10;
        @DecimalMin("0.0")
        private BigDecimal purchasePrice;
        @DecimalMin("0.0")
        private BigDecimal sellingPrice;
        private String sku;
    }

    @Data
    public static class UpdateStockRequest {
        @NotNull
        @Min(0)
        private Integer quantity;
        private String notes;
    }

    @Data
    public static class PurchaseRequest {
        @NotNull
        private Long itemId;
        @NotNull @Min(1)
        private Integer quantity;
        @NotNull @DecimalMin("0.01")
        private BigDecimal unitPrice;
        private String supplier;
        private String notes;
    }

    @Data
    public static class ItemResponse {
        private Long id;
        private String name;
        private String description;
        private String category;
        private String unit;
        private Integer quantity;
        private Integer lowStockThreshold;
        private BigDecimal purchasePrice;
        private BigDecimal sellingPrice;
        private String sku;
        private boolean lowStock;
        private boolean outOfStock;
    }
}
