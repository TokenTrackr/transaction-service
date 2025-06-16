package com.tokentrackr.transaction_service.dto.events;
import com.tokentrackr.transaction_service.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class AssetUpdateEvent {
    private String sagaId;
    private UUID transactionId;
    private String userId;
    private String cryptoId;
    private BigDecimal quantity;
    private Boolean isCompensation;
    private TransactionType transactionType;
}