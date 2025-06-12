package com.tokentrackr.transaction_service.controller;

import com.tokentrackr.transaction_service.dto.request.CreateTransactionRequest;
import com.tokentrackr.transaction_service.dto.response.TransactionResponse;
import com.tokentrackr.transaction_service.service.interfaces.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        log.info("Creating transaction for crypto: {}", request.getCryptoId());
        TransactionResponse response = transactionService.createTransaction(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getUserTransactions(Pageable pageable) {
        log.info("Fetching user transactions with pagination");
        Page<TransactionResponse> transactions = transactionService.getUserTransactions(pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable UUID id) {
        log.info("Fetching transaction by id: {}", id);
        TransactionResponse transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(transaction);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID id) {
        log.info("Deleting transaction: {}", id);
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/crypto/{cryptoId}")
    public ResponseEntity<List<TransactionResponse>> getUserTransactionsByCrypto(@PathVariable String cryptoId) {
        log.info("Fetching user transactions for crypto: {}", cryptoId);
        List<TransactionResponse> transactions = transactionService.getUserTransactionsByCrypto(cryptoId);
        return ResponseEntity.ok(transactions);
    }
}
