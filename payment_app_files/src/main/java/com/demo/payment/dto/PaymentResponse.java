package com.demo.payment.dto;

import com.demo.payment.model.TransactionStatus;
import com.demo.payment.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private String transactionId;
    private String cardNumber;
    private BigDecimal amount;
    private TransactionStatus status;
    private TransactionType type;
    private String responseCode;
    private String responseMessage;
    private String authorizationCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// Made with Bob
