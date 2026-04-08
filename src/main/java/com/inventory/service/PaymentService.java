package com.inventory.service;

import com.inventory.dto.PaymentDTO.*;
import com.inventory.entity.*;
import com.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final SaleRepository saleRepo;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    public List<PaymentRecord> getAllPayments() {
        return paymentRepo.findAll();
    }

    public List<PaymentRecord> getPendingPayments() {
        return paymentRepo.findByPaymentStatus(PaymentRecord.PaymentStatus.PENDING);
    }

    public List<PaymentRecord> getOverduePayments() {
        return paymentRepo.findOverduePayments(LocalDate.now());
    }

    public List<PaymentRecord> getPaymentsForSale(Long saleId) {
        return paymentRepo.findBySaleId(saleId);
    }

    public List<PaymentRecord> getPaymentsForClient(Long clientId) {
        return paymentRepo.findByClientIdOrderByCreatedAtDesc(clientId);
    }

    @Transactional
    public PaymentRecord markAsPaid(Long paymentId, MarkPaidRequest req) {
        PaymentRecord payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        payment.setPaymentStatus(PaymentRecord.PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());

        if (req.getUpiTransactionId() != null) {
            payment.setUpiTransactionId(req.getUpiTransactionId());
        }
        if (req.getPaymentMethod() != null) {
            payment.setPaymentMethod(PaymentRecord.PaymentMethod.valueOf(req.getPaymentMethod().toUpperCase()));
        }

        paymentRepo.save(payment);

        // Update sale payment status
        Sale sale = payment.getSale();
        List<PaymentRecord> all = paymentRepo.findBySaleId(sale.getId());
        boolean allPaid = all.stream().allMatch(p -> p.getPaymentStatus() == PaymentRecord.PaymentStatus.PAID);
        sale.setPaymentStatus(allPaid ? Sale.PaymentStatus.PAID : Sale.PaymentStatus.PARTIAL);
        saleRepo.save(sale);

        return payment;
    }

    /**
     * Creates a Razorpay order for UPI payment.
     * Note: Requires razorpay-java SDK added to pom; keeping as stub here
     * to avoid pulling heavy SDK dependency.  Swap the comment block below
     * when you add the SDK.
     */
    public CreateUpiOrderResponse createUpiOrder(Long paymentId) {
        PaymentRecord payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        // --- Razorpay SDK integration stub ---
        // RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        // JSONObject options = new JSONObject();
        // options.put("amount", payment.getAmount().multiply(BigDecimal.valueOf(100)).intValue()); // paise
        // options.put("currency", "INR");
        // options.put("receipt", payment.getSale().getSaleNumber());
        // Order order = client.orders.create(options);
        // payment.setRazorpayOrderId(order.get("id"));
        // paymentRepo.save(payment);

        // Stub response — replace with real Razorpay order once SDK added
        CreateUpiOrderResponse response = new CreateUpiOrderResponse();
        response.setOrderId("order_stub_" + paymentId);
        response.setAmount(payment.getAmount().toPlainString());
        response.setCurrency("INR");
        response.setKey(razorpayKeyId);
        response.setName("Inventory Management");
        response.setDescription("Payment for " + payment.getSale().getSaleNumber());
        return response;
    }

    @Transactional
    public void handleRazorpayWebhook(String orderId, String paymentId, String signature) {
        // Verify signature and mark paid
        paymentRepo.findAll().stream()
                .filter(p -> orderId.equals(p.getRazorpayOrderId()))
                .findFirst()
                .ifPresent(p -> {
                    p.setPaymentStatus(PaymentRecord.PaymentStatus.PAID);
                    p.setUpiTransactionId(paymentId);
                    p.setPaidAt(LocalDateTime.now());
                    paymentRepo.save(p);
                    log.info("Payment {} marked PAID via Razorpay webhook", p.getId());
                });
    }
}
