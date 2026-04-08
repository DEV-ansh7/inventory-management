package com.inventory.service;

import com.inventory.dto.SaleDTO.*;
import com.inventory.entity.*;
import com.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class SalesService {

    private final SaleRepository saleRepo;
    private final InventoryItemRepository itemRepo;
    private final ClientRepository clientRepo;
    private final PaymentRepository paymentRepo;
    private final UserRepository userRepo;
    private final InventoryService inventoryService;

    private static final AtomicLong saleCounter = new AtomicLong(System.currentTimeMillis() % 100000);

    @Transactional
    public Sale createSale(CreateSaleRequest req) {
        User user = getCurrentUser();

        Sale sale = Sale.builder()
                .saleNumber(generateSaleNumber())
                .discount(req.getDiscount() != null ? req.getDiscount() : BigDecimal.ZERO)
                .notes(req.getNotes())
                .createdBy(user)
                .build();

        if (req.getClientId() != null) {
            Client client = clientRepo.findById(req.getClientId())
                    .orElseThrow(() -> new RuntimeException("Client not found"));
            sale.setClient(client);
        }

        // Build line items and calculate total
        List<SaleItem> saleItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (SaleLineItem lineReq : req.getItems()) {
            InventoryItem item = itemRepo.findById(lineReq.getItemId())
                    .orElseThrow(() -> new RuntimeException("Item not found: " + lineReq.getItemId()));

            SaleItem si = SaleItem.builder()
                    .sale(sale)
                    .item(item)
                    .quantity(lineReq.getQuantity())
                    .unitPrice(lineReq.getUnitPrice())
                    .build();
            si.calculate();
            saleItems.add(si);
            total = total.add(si.getTotalPrice());

            // Reduce stock
            inventoryService.reduceStock(item, lineReq.getQuantity(), null, user);
        }

        sale.setItems(saleItems);
        sale.setTotalAmount(total);
        sale.setNetAmount(total.subtract(sale.getDiscount()));

        // Payment method
        PaymentRecord.PaymentMethod method = PaymentRecord.PaymentMethod.valueOf(
                req.getPaymentMethod().toUpperCase());

        if (method == PaymentRecord.PaymentMethod.DEFERRED) {
            sale.setPaymentStatus(Sale.PaymentStatus.PENDING);
        } else {
            sale.setPaymentStatus(Sale.PaymentStatus.PAID);
        }

        Sale saved = saleRepo.save(sale);

        // Fix sale reference in log for items
        saleItems.forEach(si -> si.getSale().setId(saved.getId()));

        // Create payment record
        PaymentRecord payment = PaymentRecord.builder()
                .sale(saved)
                .client(saved.getClient())
                .amount(saved.getNetAmount())
                .paymentMethod(method)
                .build();

        if (method == PaymentRecord.PaymentMethod.DEFERRED) {
            LocalDate dueDate = LocalDate.parse(req.getPaymentDueDate(), DateTimeFormatter.ISO_LOCAL_DATE);
            payment.setDueDate(dueDate);
            payment.setPaymentStatus(PaymentRecord.PaymentStatus.PENDING);
        } else {
            payment.setPaymentStatus(PaymentRecord.PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
        }

        paymentRepo.save(payment);
        return saved;
    }

    public Sale getSale(Long id) {
        return saleRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found: " + id));
    }

    public List<Sale> getAllSales() {
        return saleRepo.findAll();
    }

    public List<Sale> getPendingSales() {
        return saleRepo.findByPaymentStatus(Sale.PaymentStatus.PENDING);
    }

    public List<Sale> getSalesByClient(Long clientId) {
        return saleRepo.findByClientIdOrderBySaleDateDesc(clientId);
    }

    private String generateSaleNumber() {
        return "INV-" + LocalDate.now().getYear() + "-"
                + String.format("%05d", saleCounter.incrementAndGet());
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username).orElseThrow();
    }
}
