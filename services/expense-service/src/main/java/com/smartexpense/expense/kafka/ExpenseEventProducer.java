package com.smartexpense.expense.kafka;

import com.smartexpense.events.ExpenseCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpenseEventProducer {

    private static final String EXPENSE_CREATED_TOPIC = "expense.created";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishExpenseCreated(ExpenseCreatedEvent event) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(EXPENSE_CREATED_TOPIC, event.getUserId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Published expense.created event: expenseId={} partition={} offset={}",
                        event.getExpenseId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish expense.created event: expenseId={}",
                        event.getExpenseId(), ex);
            }
        });
    }
}