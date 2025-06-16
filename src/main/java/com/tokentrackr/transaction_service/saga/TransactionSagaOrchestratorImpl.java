package com.tokentrackr.transaction_service.saga;
import com.tokentrackr.transaction_service.dto.events.*;
import com.tokentrackr.transaction_service.entity.Transaction;
import com.tokentrackr.transaction_service.enums.TransactionStatus;
import com.tokentrackr.transaction_service.enums.TransactionType;
import com.tokentrackr.transaction_service.exception.TransactionNotFoundException;
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
                .transactionType(transaction.getTransactionType())
                .balanceUpdated(false)
                .assetUpdated(false)
                .completed(false)
                .build();

        sagaStates.put(transaction.getSagaId(), sagaState);

        // Start with appropriate step based on transaction type
        if (transaction.getTransactionType() == TransactionType.BUY) {
            publishBalanceUpdate(transaction, TransactionType.BUY);
        } else {
            publishAssetUpdate(transaction, TransactionType.SELL);
        }
    }

    public void handleBalanceUpdated(BalanceUpdatedEvent event) {
        log.info("Handling balance updated event for saga: {}", event.getSagaId());

        TransactionSagaState sagaState = sagaStates.get(event.getSagaId());
        if (sagaState == null) {
            log.error("SAGA state not found for sagaId: {}", event.getSagaId());
            return;
        }

        sagaState.setBalanceUpdated(true);

        Transaction transaction = transactionRepository.findBySagaId(event.getSagaId())
                .orElseThrow(() -> new RuntimeException("Transaction not found for sagaId: " + event.getSagaId()));

        // Determine next step based on transaction type
        if (sagaState.getTransactionType() == TransactionType.BUY) {
            // After balance update for BUY, update assets
            publishAssetUpdate(transaction, TransactionType.BUY);
        } else {
            // For SELL, balance update is the final step
            handleSagaCompletion(event.getSagaId(), event.getTransactionId());
        }
    }

    public void handleAssetUpdated(AssetUpdatedEvent event) {
        log.info("Handling asset updated event for saga: {}", event.getSagaId());

        TransactionSagaState sagaState = sagaStates.get(event.getSagaId());
        if (sagaState == null) {
            log.error("SAGA state not found for sagaId: {}", event.getSagaId());
            return;
        }

        sagaState.setAssetUpdated(true);

        Transaction transaction = transactionRepository.findBySagaId(event.getSagaId())
                .orElseThrow(() -> new RuntimeException("Transaction not found for sagaId: " + event.getSagaId()));

        // Determine next step based on transaction type
        if (sagaState.getTransactionType() == TransactionType.BUY) {
            // For BUY, asset update is the final step
            handleSagaCompletion(event.getSagaId(), event.getTransactionId());
        } else {
            // After asset update for SELL, update balance
            publishBalanceUpdate(transaction, TransactionType.SELL);
        }
    }

    public void handleBalanceUpdateFailed(BalanceUpdateFailedEvent event) {
        log.info("Handling balance update failed event for saga: {}", event.getSagaId());

        TransactionSagaState sagaState = sagaStates.get(event.getSagaId());
        if (sagaState == null) {
            log.error("SAGA state not found for sagaId: {}", event.getSagaId());
            return;
        }

        // Only compensate if we've already updated assets (SELL transaction)
        if (sagaState.getTransactionType() == TransactionType.SELL && sagaState.isAssetUpdated()) {
            compensateAssetUpdate(event.getSagaId(), event.getTransactionId());
        }

        handleSagaFailure(event.getSagaId(), event.getTransactionId(), event.getFailureReason());
    }

    public void handleAssetUpdateFailed(AssetUpdateFailedEvent event) {
        log.info("Handling asset update failed event for saga: {}", event.getSagaId());

        TransactionSagaState sagaState = sagaStates.get(event.getSagaId());
        if (sagaState == null) {
            log.error("SAGA state not found for sagaId: {}", event.getSagaId());
            return;
        }

        // Only compensate if we've already updated balance (BUY transaction)
        if (sagaState.getTransactionType() == TransactionType.BUY && sagaState.isBalanceUpdated()) {
            compensateBalanceUpdate(event.getSagaId(), event.getTransactionId());
        }

        handleSagaFailure(event.getSagaId(), event.getTransactionId(), event.getFailureReason());
    }

    private void publishBalanceUpdate(Transaction transaction, TransactionType type) {
        BalanceUpdateEvent balanceEvent = BalanceUpdateEvent.builder()
                .sagaId(transaction.getSagaId())
                .transactionId(transaction.getId())
                .userId(transaction.getUserId())
                .amount(transaction.getTotalSpent())
                .transactionType(type)
                .build();

        eventPublisher.publishBalanceUpdate(balanceEvent);
    }

    private void publishAssetUpdate(Transaction transaction, TransactionType type) {
        AssetUpdateEvent assetEvent = AssetUpdateEvent.builder()
                .sagaId(transaction.getSagaId())
                .transactionId(transaction.getId())
                .userId(transaction.getUserId())
                .cryptoId(transaction.getCryptoId())
                .quantity(transaction.getQuantity())
                .transactionType(type)
                .build();

        eventPublisher.publishAssetUpdate(assetEvent);
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
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found: " + transactionId));

        // Reverse the balance update
        TransactionType reverseType = transaction.getTransactionType() == TransactionType.BUY ?
                TransactionType.SELL : TransactionType.BUY;

        BalanceUpdateEvent compensationEvent = BalanceUpdateEvent.builder()
                .sagaId(sagaId)
                .transactionId(transactionId)
                .userId(transaction.getUserId())
                .amount(transaction.getTotalSpent())
                .transactionType(reverseType)
                .isCompensation(true)  // Add this field to your event if needed
                .build();

        eventPublisher.publishBalanceUpdate(compensationEvent);
    }

    private void compensateAssetUpdate(String sagaId, java.util.UUID transactionId) {
        log.info("Compensating asset update for transaction: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found: " + transactionId));

        // Reverse the asset update
        TransactionType reverseType = transaction.getTransactionType() == TransactionType.BUY ?
                TransactionType.SELL : TransactionType.BUY;

        AssetUpdateEvent compensationEvent = AssetUpdateEvent.builder()
                .sagaId(sagaId)
                .transactionId(transactionId)
                .userId(transaction.getUserId())
                .cryptoId(transaction.getCryptoId())
                .quantity(transaction.getQuantity())
                .transactionType(reverseType)
                .isCompensation(true)  // Add this field to your event if needed
                .build();

        eventPublisher.publishAssetUpdate(compensationEvent);
    }
}