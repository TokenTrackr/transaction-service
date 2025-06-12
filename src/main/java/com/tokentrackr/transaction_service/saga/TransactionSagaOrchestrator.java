package com.tokentrackr.transaction_service.saga;

import com.tokentrackr.transaction_service.dto.events.*;
import com.tokentrackr.transaction_service.entity.Transaction;

public interface TransactionSagaOrchestrator {

    void startTransactionSaga(Transaction transaction);

    void handleBalanceUpdated(BalanceUpdatedEvent event);

    void handleAssetUpdated(AssetUpdatedEvent event);

    void handleBalanceUpdateFailed(BalanceUpdateFailedEvent event);

    void handleAssetUpdateFailed(AssetUpdateFailedEvent event);
}
