package com.demo.payment.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TransactionRequest {

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;
}

// Made with Bob
