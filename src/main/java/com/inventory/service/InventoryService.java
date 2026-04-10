package com.inventory.service;

import com.inventory.dto.InventoryDTO.*;
import com.inventory.entity.*;
import com.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryItemRepository itemRepo;
    private final PurchaseRepository purchaseRepo;
    private final TransactionLogRepository logRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;

    public List<InventoryItem> getAllItems() {
        return itemRepo.findByActiveTrue();
    }

    public InventoryItem getItem(Long id) {
        return itemRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found: " + id));
    }

    @Transactional
    public InventoryItem createItem(CreateItemRequest req) {
        InventoryItem item = InventoryItem.builder()
                .name(req.getName())
                .description(req.getDescription())
                .category(req.getCategory())
                .unit(req.getUnit())
                .quantity(req.getQuantity())
                .lowStockThreshold(req.getLowStockThreshold())
                .purchasePrice(req.getPurchasePrice())
                .sellingPrice(req.getSellingPrice())
                .sku(req.getSku())
                .active(true)
                .build();
        return itemRepo.save(item);
    }

    @Transactional
    public InventoryItem updateItem(Long id, CreateItemRequest req) {
        InventoryItem item = getItem(id);
        item.setName(req.getName());
        item.setDescription(req.getDescription());
        item.setCategory(req.getCategory());
        item.setUnit(req.getUnit());
        item.setLowStockThreshold(req.getLowStockThreshold());
        item.setPurchasePrice(req.getPurchasePrice());
        item.setSellingPrice(req.getSellingPrice());
        item.setSku(req.getSku());
        return itemRepo.save(item);
    }

    @Transactional
    public Purchase recordPurchase(PurchaseRequest req) {
        InventoryItem item = getItem(req.getItemId());
        User user = getCurrentUser();

        int before = item.getQuantity();
        item.setQuantity(before + req.getQuantity());
        itemRepo.save(item);

        Purchase purchase = Purchase.builder()
                .item(item)
                .quantity(req.getQuantity())
                .unitPrice(req.getUnitPrice())
                .supplier(req.getSupplier())
                .notes(req.getNotes())
                .createdBy(user)
                .build();
        purchaseRepo.save(purchase);

        logTransaction(TransactionLog.TransactionType.PURCHASE, item,
                req.getQuantity(), before, item.getQuantity(),
                req.getUnitPrice(), purchase.getId(), "PURCHASE", user, req.getNotes());

        return purchase;
    }

    @Transactional
    public void adjustStock(Long itemId, UpdateStockRequest req) {
        InventoryItem item = getItem(itemId);
        User user = getCurrentUser();
        int before = item.getQuantity();
        item.setQuantity(req.getQuantity());
        itemRepo.save(item);

        logTransaction(TransactionLog.TransactionType.STOCK_ADJUSTMENT, item,
                req.getQuantity() - before, before, req.getQuantity(),
                null, itemId, "ADJUSTMENT", user, req.getNotes());
    }

    public void deleteItem(Long id) {
        InventoryItem item = getItem(id);
        item.setActive(false);
        itemRepo.save(item);
    }

    public List<InventoryItem> getLowStockItems() {
        return itemRepo.findLowStockItems();
    }

    public List<InventoryItem> searchItems(String name) {
        return itemRepo.findByNameContainingIgnoreCaseAndActiveTrue(name);
    }

    // Internal: reduce stock after a sale (called from SalesService)
    @Transactional
    public void reduceStock(InventoryItem item, int qty, Long saleId, User user) {
        int before = item.getQuantity();
        if (before < qty) {
            throw new RuntimeException("Insufficient stock for item: " + item.getName()
                    + " (available: " + before + ", requested: " + qty + ")");
        }
        item.setQuantity(before - qty);
        itemRepo.save(item);

        logTransaction(TransactionLog.TransactionType.SALE, item,
                -qty, before, item.getQuantity(),
                item.getSellingPrice(), saleId, "SALE", user, null);

        // check if stock fell to low level
        if (item.getQuantity() <= item.getLowStockThreshold()) {
            notificationService.sendLowStockAlert(item.getName(), item.getQuantity(), item.getLowStockThreshold());
        }
    }

    private void logTransaction(TransactionLog.TransactionType type, InventoryItem item,
                                 int change, int before, int after,
                                 java.math.BigDecimal unitPrice,
                                 Long refId, String refType, User user, String notes) {
        TransactionLog log = TransactionLog.builder()
                .transactionType(type)
                .item(item)
                .itemName(item.getName())
                .quantityChange(change)
                .quantityBefore(before)
                .quantityAfter(after)
                .unitPrice(unitPrice)
                .referenceId(refId)
                .referenceType(refType)
                .performedBy(user)
                .notes(notes)
                .build();
        logRepo.save(log);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username).orElseThrow();
    }
}
