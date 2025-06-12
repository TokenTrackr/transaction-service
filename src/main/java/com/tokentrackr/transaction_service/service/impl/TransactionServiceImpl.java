package com.tokentrackr.transaction_service.service.impl;
import com.tokentrackr.transaction_service.dto.request.CreateTransactionRequest;
import com.tokentrackr.transaction_service.dto.response.TransactionResponse;
import com.tokentrackr.transaction_service.entity.Transaction;
import com.tokentrackr.transaction_service.enums.TransactionStatus;
import com.tokentrackr.transaction_service.exception.TransactionNotFoundException;
import com.tokentrackr.transaction_service.mapper.TransactionMapper;
import com.tokentrackr.transaction_service.repository.TransactionRepository;
import com.tokentrackr.transaction_service.saga.TransactionSagaOrchestrator;
import com.tokentrackr.transaction_service.service.interfaces.TransactionService;
import com.tokentrackr.transaction_service.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final TransactionSagaOrchestrator sagaOrchestrator;

    @Override
    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        String userId = SecurityUtil.getCurrentUserId();
        log.info("Creating transaction for user: {}", userId);

        Transaction transaction = Transaction.builder()
                .cryptoId(request.getCryptoId())
                .userId(userId)
                .transactionType(request.getTransactionType())
                .quantity(request.getQuantity())
                .totalSpent(request.getTotalSpent())
                .pricePerCoin(request.getPricePerCoin())
                .status(TransactionStatus.PENDING)
                .sagaId(UUID.randomUUID().toString())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created with ID: {}", savedTransaction.getId());

        // Start SAGA orchestration
        sagaOrchestrator.startTransactionSaga(savedTransaction);

        return transactionMapper.toResponse(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getUserTransactions(Pageable pageable) {
        String userId = SecurityUtil.getCurrentUserId();
        log.info("Fetching transactions for user: {}", userId);

        Page<Transaction> transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return transactions.map(transactionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID id) {
        String userId = SecurityUtil.getCurrentUserId();
        log.info("Fetching transaction {} for user: {}", id, userId);

        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with id: " + id));

        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional
    public void deleteTransaction(UUID id) {
        String userId = SecurityUtil.getCurrentUserId();
        log.info("Deleting transaction {} for user: {}", id, userId);

        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with id: " + id));

        if (transaction.getStatus() == TransactionStatus.PENDING) {
            throw new IllegalStateException("Cannot delete pending transaction");
        }

        transactionRepository.delete(transaction);
        log.info("Transaction {} deleted successfully", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getUserTransactionsByCrypto(String cryptoId) {
        String userId = SecurityUtil.getCurrentUserId();
        log.info("Fetching transactions for user: {} and crypto: {}", userId, cryptoId);

        List<Transaction> transactions = transactionRepository.findByUserIdAndCryptoIdOrderByCreatedAtDesc(userId, cryptoId);
        return transactions.stream()
                .map(transactionMapper::toResponse)
                .toList();
    }
}
