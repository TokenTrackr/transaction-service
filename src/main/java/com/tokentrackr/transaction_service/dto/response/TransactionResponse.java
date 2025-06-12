package com.tokentrackr.transaction_service.dto.response;

import com.tokentrackr.transaction_service.enums.TransactionStatus;
import com.tokentrackr.transaction_service.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TransactionResponse {
    private UUID id;
    private String cryptoId;
    private String userId;
    private TransactionType transactionType;
    private BigDecimal quantity;
    private BigDecimal totalSpent;
    private BigDecimal pricePerCoin;
    private TransactionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String failureReason;
}
