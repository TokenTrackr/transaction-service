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

    // 1) Command messages → Direct Exchange
    @Override
    public void publishBalanceUpdate(BalanceUpdateEvent event) {
        log.info("Publishing balance update for saga: {}", event.getSagaId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SAGA_DIRECT_EXCHANGE,     // DirectExchange
                "balance.update",                       // routing key
                event
        );
    }

    @Override
    public void publishAssetUpdate(AssetUpdateEvent event) {
        log.info("Publishing asset update for saga: {}", event.getSagaId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SAGA_DIRECT_EXCHANGE,
                "asset.update",
                event
        );
    }

    // 2) Outcome events → Topic Exchange
    @Override
    public void publishTransactionCompleted(TransactionCompletedEvent event) {
        log.info("Publishing transaction completed for saga: {}", event.getSagaId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SAGA_TOPIC_EXCHANGE,     // TopicExchange
                "transaction.completed",                // routing key
                event
        );
    }

    @Override
    public void publishTransactionFailed(TransactionFailedEvent event) {
        log.info("Publishing transaction failed for saga: {}", event.getSagaId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SAGA_TOPIC_EXCHANGE,
                "transaction.failed",
                event
        );
    }
}
