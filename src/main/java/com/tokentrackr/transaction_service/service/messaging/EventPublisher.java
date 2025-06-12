package com.tokentrackr.transaction_service.service.messaging;

import com.tokentrackr.transaction_service.dto.events.*;

public interface EventPublisher {
    void publishBalanceUpdate(BalanceUpdateEvent event);
    void publishAssetUpdate(AssetUpdateEvent event);
    void publishTransactionCompleted(TransactionCompletedEvent event);
    void publishTransactionFailed(TransactionFailedEvent event);
}
