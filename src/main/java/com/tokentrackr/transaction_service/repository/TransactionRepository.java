package com.tokentrackr.transaction_service.repository;
import com.tokentrackr.transaction_service.entity.Transaction;
import com.tokentrackr.transaction_service.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Page<Transaction> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    List<Transaction> findByUserIdAndStatus(String userId, TransactionStatus status);

    Optional<Transaction> findByIdAndUserId(UUID id, String userId);

    Optional<Transaction> findBySagaId(String sagaId);

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND t.cryptoId = :cryptoId ORDER BY t.createdAt DESC")
    List<Transaction> findByUserIdAndCryptoIdOrderByCreatedAtDesc(@Param("userId") String userId, @Param("cryptoId") String cryptoId);

    long countByUserIdAndStatus(String userId, TransactionStatus status);
}
