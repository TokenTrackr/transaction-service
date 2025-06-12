package com.tokentrackr.transaction_service.service.interfaces;

import com.tokentrackr.transaction_service.dto.request.CreateTransactionRequest;
import com.tokentrackr.transaction_service.dto.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
    TransactionResponse createTransaction(CreateTransactionRequest request);
    Page<TransactionResponse> getUserTransactions(Pageable pageable);
    TransactionResponse getTransactionById(UUID id);
    void deleteTransaction(UUID id);
    List<TransactionResponse> getUserTransactionsByCrypto(String cryptoId);
}
