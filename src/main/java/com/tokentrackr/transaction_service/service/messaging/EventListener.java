package com.tokentrackr.transaction_service.service.messaging;

import com.tokentrackr.transaction_service.dto.events.*;

public interface EventListener {
    void handleBalanceUpdated(BalanceUpdatedEvent event);
    void handleAssetUpdated(AssetUpdatedEvent event);
    void handleBalanceUpdateFailed(BalanceUpdateFailedEvent event);
    void handleAssetUpdateFailed(AssetUpdateFailedEvent event);
}
