package com.tokentrackr.transaction_service.dto.events;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TransactionFailedEvent {
    private String sagaId;
    private UUID transactionId;
    private String userId;
    private String failureReason;
}
