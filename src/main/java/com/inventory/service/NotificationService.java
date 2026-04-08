package com.inventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    public void sendPaymentDueAlert(String clientEmail, String clientName,
                                     String saleNumber, String amount, String dueDate) {
        String subject = "[Action Required] Payment Due - " + saleNumber;
        String body = String.format("""
                Dear %s,

                This is a reminder that your payment of ₹%s for order %s is due on %s.

                Please arrange payment at your earliest convenience.

                Regards,
                Inventory Management Team
                """, clientName, amount, saleNumber, dueDate);

        sendEmail(clientEmail, subject, body);
        sendEmail(adminEmail, "[Alert] Payment Due: " + saleNumber,
                "Payment of ₹" + amount + " from " + clientName + " is due on " + dueDate);
    }

    public void sendPaymentOverdueAlert(String clientEmail, String clientName,
                                         String saleNumber, String amount, String dueDate) {
        String subject = "[OVERDUE] Payment Overdue - " + saleNumber;
        String body = String.format("""
                Dear %s,

                Your payment of ₹%s for order %s was due on %s and is now OVERDUE.

                Please make the payment immediately to avoid further action.

                Regards,
                Inventory Management Team
                """, clientName, amount, saleNumber, dueDate);

        sendEmail(clientEmail, subject, body);
        sendEmail(adminEmail, "[OVERDUE Alert] " + saleNumber,
                "OVERDUE: ₹" + amount + " from " + clientName + " (due " + dueDate + ")");
    }

    public void sendLowStockAlert(String itemName, int currentQty, int threshold) {
        String subject = "[Stock Alert] Low Stock: " + itemName;
        String body = String.format("""
                Stock Alert!

                Item: %s
                Current Quantity: %d
                Threshold: %d

                Please restock immediately.

                Regards,
                Inventory Management System
                """, itemName, currentQty, threshold);

        sendEmail(adminEmail, subject, body);
    }
}
