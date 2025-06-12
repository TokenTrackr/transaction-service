package com.tokentrackr.transaction_service.service.messaging.impl;
import com.tokentrackr.transaction_service.dto.events.*;
import com.tokentrackr.transaction_service.saga.TransactionSagaOrchestrator;
import com.tokentrackr.transaction_service.service.messaging.EventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQEventListener implements EventListener {

    private final TransactionSagaOrchestrator sagaOrchestrator;

    @Override
    @RabbitListener(queues = "balance.updated.queue")
    public void handleBalanceUpdated(BalanceUpdatedEvent event) {
        log.info("Received balance updated event for saga: {}", event.getSagaId());
        sagaOrchestrator.handleBalanceUpdated(event);
    }

    @Override
    @RabbitListener(queues = "asset.updated.queue")
    public void handleAssetUpdated(AssetUpdatedEvent event) {
        log.info("Received asset updated event for saga: {}", event.getSagaId());
        sagaOrchestrator.handleAssetUpdated(event);
    }

    @Override
    @RabbitListener(queues = "balance.update.failed.queue")
    public void handleBalanceUpdateFailed(BalanceUpdateFailedEvent event) {
        log.info("Received balance update failed event for saga: {}", event.getSagaId());
        sagaOrchestrator.handleBalanceUpdateFailed(event);
    }

    @Override
    @RabbitListener(queues = "asset.update.failed.queue")
    public void handleAssetUpdateFailed(AssetUpdateFailedEvent event) {
        log.info("Received asset update failed event for saga: {}", event.getSagaId());
        sagaOrchestrator.handleAssetUpdateFailed(event);
    }
}
