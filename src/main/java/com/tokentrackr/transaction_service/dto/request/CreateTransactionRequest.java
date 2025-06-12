package com.tokentrackr.transaction_service.dto.request;
import com.tokentrackr.transaction_service.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CreateTransactionRequest {

    @NotBlank(message = "Crypto ID is required")
    private String cryptoId;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.00000001", message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @NotNull(message = "Total spent is required")
    @DecimalMin(value = "0.01", message = "Total spent must be greater than 0")
    private BigDecimal totalSpent;

    @NotNull(message = "Price per coin is required")
    @DecimalMin(value = "0.00000001", message = "Price per coin must be greater than 0")
    private BigDecimal pricePerCoin;
}
