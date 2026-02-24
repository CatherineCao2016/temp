package com.demo.payment.service;

import com.demo.payment.dto.PaymentRequest;
import com.demo.payment.dto.PaymentResponse;
import com.demo.payment.dto.TransactionRequest;
import com.demo.payment.model.Transaction;
import com.demo.payment.model.TransactionStatus;
import com.demo.payment.model.TransactionType;
import com.demo.payment.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final Random random = new Random();
    private final List<String> validCardNumbers;

    public PaymentService(TransactionRepository transactionRepository,
                         @Value("${payment.test.cards:}") String testCards) {
        this.transactionRepository = transactionRepository;
        this.validCardNumbers = testCards.isEmpty()
            ? List.of()
            : Arrays.asList(testCards.split(","));
        
        if (validCardNumbers.isEmpty()) {
            log.warn("No test card numbers configured. All card validations will fail.");
        }
    }

    @Transactional
    public PaymentResponse authorize(PaymentRequest request) {
        log.info("Processing authorization for card ending in {}", maskCardNumber(request.getCardNumber()));

        // Simulate processing delay (200-500ms)
        simulateProcessingDelay();

        // Validate card
        String validationResult = validateCard(request);
        if (validationResult != null) {
            return createDeclinedTransaction(request, validationResult);
        }

        // Simulate random declines (10% of transactions)
        if (random.nextInt(100) < 10) {
            return createDeclinedTransaction(request, "DECLINED");
        }

        // Create successful authorization
        Transaction transaction = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .cardNumber(maskCardNumber(request.getCardNumber()))
                .cardExpiry(request.getCardExpiry())
                .amount(request.getAmount())
                .status(TransactionStatus.AUTHORIZED)
                .type(TransactionType.AUTHORIZE)
                .responseCode("00")
                .responseMessage("Approved")
                .authorizationCode(generateAuthCode())
                .build();

        transaction = transactionRepository.save(transaction);
        log.info("Authorization successful: {}", transaction.getTransactionId());

        return mapToResponse(transaction);
    }

    @Transactional
    public PaymentResponse capture(TransactionRequest request) {
        log.info("Processing capture for transaction: {}", request.getTransactionId());

        // Simulate processing delay
        simulateProcessingDelay();

        Transaction transaction = transactionRepository.findByTransactionId(request.getTransactionId())
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (transaction.getStatus() != TransactionStatus.AUTHORIZED) {
            throw new IllegalStateException("Transaction must be in AUTHORIZED status to capture");
        }

        transaction.setStatus(TransactionStatus.CAPTURED);
        transaction.setType(TransactionType.CAPTURE);
        transaction.setResponseCode("00");
        transaction.setResponseMessage("Captured");

        transaction = transactionRepository.save(transaction);
        log.info("Capture successful: {}", transaction.getTransactionId());

        return mapToResponse(transaction);
    }

    @Transactional
    public PaymentResponse refund(TransactionRequest request) {
        log.info("Processing refund for transaction: {}", request.getTransactionId());

        // Simulate processing delay
        simulateProcessingDelay();

        Transaction transaction = transactionRepository.findByTransactionId(request.getTransactionId())
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (transaction.getStatus() != TransactionStatus.CAPTURED) {
            throw new IllegalStateException("Transaction must be in CAPTURED status to refund");
        }

        transaction.setStatus(TransactionStatus.REFUNDED);
        transaction.setType(TransactionType.REFUND);
        transaction.setResponseCode("00");
        transaction.setResponseMessage("Refunded");

        transaction = transactionRepository.save(transaction);
        log.info("Refund successful: {}", transaction.getTransactionId());

        return mapToResponse(transaction);
    }

    @Cacheable(value = "transactions", key = "#id")
    public PaymentResponse getTransaction(Long id) {
        log.info("Fetching transaction by ID: {}", id);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
        return mapToResponse(transaction);
    }

    public List<PaymentResponse> getTransactionHistory() {
        log.info("Fetching transaction history");
        return transactionRepository.findTop20ByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "transactions", allEntries = true)
    public void clearCache() {
        log.info("Cache cleared");
    }

    private PaymentResponse createDeclinedTransaction(PaymentRequest request, String reason) {
        String responseMessage;
        switch (reason) {
            case "EXPIRED":
                responseMessage = "Card expired";
                break;
            case "INSUFFICIENT_FUNDS":
                responseMessage = "Insufficient funds";
                break;
            case "INVALID_CARD":
                responseMessage = "Invalid card number";
                break;
            default:
                responseMessage = "Transaction declined";
                break;
        }

        Transaction transaction = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .cardNumber(maskCardNumber(request.getCardNumber()))
                .cardExpiry(request.getCardExpiry())
                .amount(request.getAmount())
                .status(TransactionStatus.DECLINED)
                .type(TransactionType.AUTHORIZE)
                .responseCode(reason)
                .responseMessage(responseMessage)
                .build();

        transaction = transactionRepository.save(transaction);
        log.info("Transaction declined: {} - {}", transaction.getTransactionId(), responseMessage);

        return mapToResponse(transaction);
    }

    private String validateCard(PaymentRequest request) {
        // Check if card number is in the valid test cards list
        if (!validCardNumbers.contains(request.getCardNumber())) {
            return "INVALID_CARD";
        }

        // Check if card is expired
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
            YearMonth cardExpiry = YearMonth.parse(request.getCardExpiry(), formatter);
            YearMonth now = YearMonth.now();
            
            if (cardExpiry.isBefore(now)) {
                return "EXPIRED";
            }
        } catch (Exception e) {
            return "INVALID_CARD";
        }

        return null;
    }

    private void simulateProcessingDelay() {
        try {
            // Random delay between 200-500ms
            int delay = 200 + random.nextInt(301);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber.length() < 4) {
            return cardNumber;
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    private String generateAuthCode() {
        return String.format("%06d", random.nextInt(1000000));
    }

    private PaymentResponse mapToResponse(Transaction transaction) {
        return PaymentResponse.builder()
                .id(transaction.getId())
                .transactionId(transaction.getTransactionId())
                .cardNumber(transaction.getCardNumber())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .type(transaction.getType())
                .responseCode(transaction.getResponseCode())
                .responseMessage(transaction.getResponseMessage())
                .authorizationCode(transaction.getAuthorizationCode())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}

// Made with Bob
