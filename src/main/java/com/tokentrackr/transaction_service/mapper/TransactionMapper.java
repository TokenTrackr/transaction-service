package com.tokentrackr.transaction_service.mapper;

import com.tokentrackr.transaction_service.dto.response.TransactionResponse;
import com.tokentrackr.transaction_service.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .cryptoId(transaction.getCryptoId())
                .userId(transaction.getUserId())
                .transactionType(transaction.getTransactionType())
                .quantity(transaction.getQuantity())
                .totalSpent(transaction.getTotalSpent())
                .pricePerCoin(transaction.getPricePerCoin())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .failureReason(transaction.getFailureReason())
                .build();
    }
}
