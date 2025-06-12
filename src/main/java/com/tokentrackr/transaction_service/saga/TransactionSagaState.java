package com.tokentrackr.transaction_service.saga;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionSagaState {
    private String sagaId;
    private boolean balanceUpdated;
    private boolean assetUpdated;
    private boolean completed;
    private String failureReason;
}
