package com.smartexpense.analytics.kafka;

import com.smartexpense.events.ExpenseCreatedEvent;
import com.smartexpense.analytics.service.AnalyticsService;
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

    private final AnalyticsService analyticsService;

    @KafkaListener(
            topics = "expense.created",
            groupId = "analytics-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleExpenseCreated(
            @Payload ExpenseCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        log.info("Received expense.created event: expenseId={} partition={} offset={}",
                event.getExpenseId(), partition, offset);

        try {
            analyticsService.processExpenseCreated(event);
            ack.acknowledge();
            log.info("Processed and acknowledged: expenseId={}", event.getExpenseId());
        } catch (Exception e) {
            log.error("Failed to process expense.created event: expenseId={}",
                    event.getExpenseId(), e);
        }
    }
}