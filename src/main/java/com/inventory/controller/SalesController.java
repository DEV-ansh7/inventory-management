package com.inventory.controller;

import com.inventory.dto.ApiResponse;
import com.inventory.dto.SaleDTO.*;
import com.inventory.entity.Sale;
import com.inventory.service.SalesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SalesController {

    private final SalesService salesService;

    @PostMapping
    public ResponseEntity<ApiResponse<Sale>> createSale(@Valid @RequestBody CreateSaleRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Sale created", salesService.createSale(req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Sale>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(salesService.getAllSales()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Sale>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(salesService.getSale(id)));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<Sale>>> getPending() {
        return ResponseEntity.ok(ApiResponse.ok(salesService.getPendingSales()));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<ApiResponse<List<Sale>>> getByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(ApiResponse.ok(salesService.getSalesByClient(clientId)));
    }
}
