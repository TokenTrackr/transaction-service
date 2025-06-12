package com.tokentrackr.transaction_service.service.messaging.impl;

import com.tokentrackr.transaction_service.config.RabbitMQConfig;
import com.tokentrackr.transaction_service.dto.events.*;
import com.tokentrackr.transaction_service.service.messaging.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQEventPublisher implements EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishBalanceUpdate(BalanceUpdateEvent event) {
        log.info("Publishing balance update event for saga: {}", event.getSagaId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.TRANSACTION_SAGA_EXCHANGE,
                "balance.update",
                event
        );
    }

    @Override
    public void publishAssetUpdate(AssetUpdateEvent event) {
        log.info("Publishing asset update event for saga: {}", event.getSagaId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.TRANSACTION_SAGA_EXCHANGE,
                "asset.update",
                event
        );
    }

    @Override
    public void publishTransactionCompleted(TransactionCompletedEvent event) {
        log.info("Publishing transaction completed event for saga: {}", event.getSagaId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.TRANSACTION_SAGA_EXCHANGE,
                "transaction.completed",
                event
        );
    }

    @Override
    public void publishTransactionFailed(TransactionFailedEvent event) {
        log.info("Publishing transaction failed event for saga: {}", event.getSagaId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.TRANSACTION_SAGA_EXCHANGE,
                "transaction.failed",
                event
        );
    }
}
