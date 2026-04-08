package com.inventory.controller;

import com.inventory.dto.ApiResponse;
import com.inventory.dto.InventoryDTO.*;
import com.inventory.entity.*;
import com.inventory.repository.TransactionLogRepository;
import com.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InventoryController {

    private final InventoryService inventoryService;
    private final TransactionLogRepository logRepo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getAllItems()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryItem>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getItem(id)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> search(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.searchItems(q)));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getLowStock() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getLowStockItems()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryItem>> create(@Valid @RequestBody CreateItemRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Item created", inventoryService.createItem(req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryItem>> update(@PathVariable Long id,
                                                              @Valid @RequestBody CreateItemRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Item updated", inventoryService.updateItem(id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        inventoryService.deleteItem(id);
        return ResponseEntity.ok(ApiResponse.ok("Item deleted", null));
    }

    @PostMapping("/purchase")
    public ResponseEntity<ApiResponse<Purchase>> recordPurchase(@Valid @RequestBody PurchaseRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Purchase recorded", inventoryService.recordPurchase(req)));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<Void>> adjustStock(@PathVariable Long id,
                                                          @RequestBody UpdateStockRequest req) {
        inventoryService.adjustStock(id, req);
        return ResponseEntity.ok(ApiResponse.ok("Stock updated", null));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<?>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                logRepo.findAllByOrderByTransactionDateDesc(
                        PageRequest.of(page, size, Sort.by("transactionDate").descending()))));
    }

    @GetMapping("/transactions/item/{itemId}")
    public ResponseEntity<ApiResponse<List<TransactionLog>>> getItemTransactions(@PathVariable Long itemId) {
        return ResponseEntity.ok(ApiResponse.ok(logRepo.findByItemIdOrderByTransactionDateDesc(itemId)));
    }
}
