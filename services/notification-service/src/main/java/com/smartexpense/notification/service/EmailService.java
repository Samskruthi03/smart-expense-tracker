package com.smartexpense.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendExpenseConfirmation(String toEmail,
                                        String firstName,
                                        String amount,
                                        String category,
                                        String description) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Expense recorded — " + amount + " EUR");
            message.setText(String.format("""
                Hi %s,

                Your expense has been recorded successfully.

                Amount:      %s EUR
                Category:    %s
                Description: %s

                You can view all your expenses at http://localhost:3000/expenses

                — Smart Expense Tracker
                """, firstName, amount, category, description));

            mailSender.send(message);
            log.info("Sent expense confirmation email to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }
}