package com.tokentrackr.transaction_service.saga;

import com.tokentrackr.transaction_service.dto.events.*;
import com.tokentrackr.transaction_service.entity.Transaction;
import com.tokentrackr.transaction_service.enums.TransactionStatus;
import com.tokentrackr.transaction_service.repository.TransactionRepository;
import com.tokentrackr.transaction_service.service.messaging.EventPublisher;



import java.util.concurrent.ConcurrentHashMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionSagaOrchestratorImpl implements TransactionSagaOrchestrator {
    private final EventPublisher eventPublisher;
    private final TransactionRepository transactionRepository;
    private final ConcurrentHashMap<String, TransactionSagaState> sagaStates = new ConcurrentHashMap<>();

    public void startTransactionSaga(Transaction transaction) {
        log.info("Starting SAGA for transaction: {}", transaction.getId());

        TransactionSagaState sagaState = TransactionSagaState.builder()
                .sagaId(transaction.getSagaId())
                .balanceUpdated(false)
                .assetUpdated(false)
                .completed(false)
                .build();

        sagaStates.put(transaction.getSagaId(), sagaState);

        // Start with balance update
        BalanceUpdateEvent balanceEvent = BalanceUpdateEvent.builder()
                .sagaId(transaction.getSagaId())
                .transactionId(transaction.getId())
                .userId(transaction.getUserId())
                .amount(transaction.getTotalSpent())
                .transactionType(transaction.getTransactionType())
                .build();

        eventPublisher.publishBalanceUpdate(balanceEvent);
    }

    public void handleBalanceUpdated(BalanceUpdatedEvent event) {
        log.info("Handling balance updated event for saga: {}", event.getSagaId());

        TransactionSagaState sagaState = sagaStates.get(event.getSagaId());
        if (sagaState == null) {
            log.error("SAGA state not found for sagaId: {}", event.getSagaId());
            return;
        }

        if (event.isSuccess()) {
            sagaState.setBalanceUpdated(true);

            // Proceed to asset update
            Transaction transaction = transactionRepository.findBySagaId(event.getSagaId())
                    .orElseThrow(() -> new RuntimeException("Transaction not found for sagaId: " + event.getSagaId()));

            AssetUpdateEvent assetEvent = AssetUpdateEvent.builder()
                    .sagaId(event.getSagaId())
                    .transactionId(event.getTransactionId())
                    .userId(event.getUserId())
                    .cryptoId(transaction.getCryptoId())
                    .quantity(transaction.getQuantity())
                    .transactionType(transaction.getTransactionType())
                    .build();

            eventPublisher.publishAssetUpdate(assetEvent);
        } else {
            handleSagaFailure(event.getSagaId(), event.getTransactionId(), event.getFailureReason());
        }
    }

    public void handleAssetUpdated(AssetUpdatedEvent event) {
        log.info("Handling asset updated event for saga: {}", event.getSagaId());

        TransactionSagaState sagaState = sagaStates.get(event.getSagaId());
        if (sagaState == null) {
            log.error("SAGA state not found for sagaId: {}", event.getSagaId());
            return;
        }

        if (event.isSuccess()) {
            sagaState.setAssetUpdated(true);
            handleSagaCompletion(event.getSagaId(), event.getTransactionId());
        } else {
            // Need to compensate balance update
            compensateBalanceUpdate(event.getSagaId(), event.getTransactionId());
            handleSagaFailure(event.getSagaId(), event.getTransactionId(), event.getFailureReason());
        }
    }

    public void handleBalanceUpdateFailed(BalanceUpdateFailedEvent event) {
        log.info("Handling balance update failed event for saga: {}", event.getSagaId());
        handleSagaFailure(event.getSagaId(), event.getTransactionId(), event.getFailureReason());
    }

    public void handleAssetUpdateFailed(AssetUpdateFailedEvent event) {
        log.info("Handling asset update failed event for saga: {}", event.getSagaId());
        compensateBalanceUpdate(event.getSagaId(), event.getTransactionId());
        handleSagaFailure(event.getSagaId(), event.getTransactionId(), event.getFailureReason());
    }

    private void handleSagaCompletion(String sagaId, java.util.UUID transactionId) {
        log.info("Completing SAGA for transaction: {}", transactionId);

        // Update transaction status
        transactionRepository.findById(transactionId).ifPresent(transaction -> {
            transaction.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);
        });

        // Publish completion event
        TransactionCompletedEvent completedEvent = TransactionCompletedEvent.builder()
                .sagaId(sagaId)
                .transactionId(transactionId)
                .build();

        eventPublisher.publishTransactionCompleted(completedEvent);

        // Cleanup saga state
        sagaStates.remove(sagaId);
    }

    private void handleSagaFailure(String sagaId, java.util.UUID transactionId, String failureReason) {
        log.info("Failing SAGA for transaction: {} with reason: {}", transactionId, failureReason);

        // Update transaction status
        transactionRepository.findById(transactionId).ifPresent(transaction -> {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason(failureReason);
            transactionRepository.save(transaction);
        });

        // Publish failure event
        TransactionFailedEvent failedEvent = TransactionFailedEvent.builder()
                .sagaId(sagaId)
                .transactionId(transactionId)
                .failureReason(failureReason)
                .build();

        eventPublisher.publishTransactionFailed(failedEvent);

        // Cleanup saga state
        sagaStates.remove(sagaId);
    }

    private void compensateBalanceUpdate(String sagaId, java.util.UUID transactionId) {
        log.info("Compensating balance update for transaction: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));

        // Reverse the balance update
        BalanceUpdateEvent compensationEvent = BalanceUpdateEvent.builder()
                .sagaId(sagaId)
                .transactionId(transactionId)
                .userId(transaction.getUserId())
                .amount(transaction.getTotalSpent())
                .transactionType(transaction.getTransactionType() == com.tokentrackr.transaction_service.enums.TransactionType.BUY ?
                        com.tokentrackr.transaction_service.enums.TransactionType.SELL : com.tokentrackr.transaction_service.enums.TransactionType.BUY)
                .build();

        eventPublisher.publishBalanceUpdate(compensationEvent);
    }
}
