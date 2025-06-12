package com.tokentrackr.transaction_service.dto.events;
import com.tokentrackr.transaction_service.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class TransactionCreatedEvent {
    private UUID transactionId;
    private String sagaId;
    private String userId;
    private String cryptoId;
    private TransactionType transactionType;
    private BigDecimal quantity;
    private BigDecimal totalSpent;
    private BigDecimal pricePerCoin;
}
