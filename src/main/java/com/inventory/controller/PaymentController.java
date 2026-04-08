package com.inventory.controller;

import com.inventory.dto.ApiResponse;
import com.inventory.dto.PaymentDTO.*;
import com.inventory.entity.PaymentRecord;
import com.inventory.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentRecord>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getAllPayments()));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentRecord>>> getPending() {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getPendingPayments()));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentRecord>>> getOverdue() {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getOverduePayments()));
    }

    @GetMapping("/sale/{saleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentRecord>>> getBySale(@PathVariable Long saleId) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getPaymentsForSale(saleId)));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentRecord>>> getByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getPaymentsForClient(clientId)));
    }

    @PostMapping("/{id}/mark-paid")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentRecord>> markPaid(@PathVariable Long id,
                                                                @RequestBody MarkPaidRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Marked as paid", paymentService.markAsPaid(id, req)));
    }

    @GetMapping("/{id}/upi-order")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CreateUpiOrderResponse>> createUpiOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.createUpiOrder(id)));
    }

    // Razorpay webhook — public endpoint
    @PostMapping("/webhook/razorpay")
    public ResponseEntity<Void> razorpayWebhook(@RequestBody Map<String, Object> payload) {
        try {
            Map<String, Object> entity = (Map<String, Object>) ((Map<String, Object>) payload.get("payload")).get("payment");
            Map<String, Object> paymentEntity = (Map<String, Object>) entity.get("entity");
            String orderId = (String) paymentEntity.get("order_id");
            String paymentId = (String) paymentEntity.get("id");
            paymentService.handleRazorpayWebhook(orderId, paymentId, null);
        } catch (Exception e) {
            // Log but don't fail — always return 200 to Razorpay
        }
        return ResponseEntity.ok().build();
    }
}
