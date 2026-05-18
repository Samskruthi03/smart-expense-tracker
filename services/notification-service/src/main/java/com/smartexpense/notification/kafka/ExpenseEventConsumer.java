package com.smartexpense.notification.kafka;

import com.smartexpense.events.ExpenseCreatedEvent;
import com.smartexpense.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpenseEventConsumer {

    private final EmailService emailService;

    @KafkaListener(
            topics = "expense.created",
            groupId = "notification-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleExpenseCreated(
            @Payload ExpenseCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        log.info("Received expense.created: expenseId={} partition={} offset={}",
                event.getExpenseId(), partition, offset);

        try {
            emailService.sendExpenseConfirmation(
                    event.getUserId() + "@placeholder.com",
                    "User",
                    event.getAmount().toPlainString(),
                    event.getCategory(),
                    event.getDescription() != null ? event.getDescription() : "N/A");

            ack.acknowledge();
            log.info("Processed notification for expenseId={}", event.getExpenseId());
        } catch (Exception e) {
            log.error("Failed to process notification: expenseId={}",
                    event.getExpenseId(), e);
        }
    }
}