package com.demo.payment.controller;

import com.demo.payment.dto.PaymentRequest;
import com.demo.payment.dto.PaymentResponse;
import com.demo.payment.dto.TransactionRequest;
import com.demo.payment.service.PaymentService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"${payment.cors.allowed-origins:http://localhost:3000,http://localhost:8080}"})
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/authorize")
    public ResponseEntity<PaymentResponse> authorize(@Valid @RequestBody PaymentRequest request) {
        log.info("Received authorization request");
        PaymentResponse response = paymentService.authorize(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/capture")
    public ResponseEntity<PaymentResponse> capture(@Valid @RequestBody TransactionRequest request) {
        log.info("Received capture request for transaction: {}", request.getTransactionId());
        try {
            PaymentResponse response = paymentService.capture(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Capture failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/refund")
    public ResponseEntity<PaymentResponse> refund(@Valid @RequestBody TransactionRequest request) {
        log.info("Received refund request for transaction: {}", request.getTransactionId());
        try {
            PaymentResponse response = paymentService.refund(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Refund failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getTransaction(@PathVariable Long id) {
        log.info("Received request to fetch transaction: {}", id);
        try {
            PaymentResponse response = paymentService.getTransaction(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Transaction not found: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<PaymentResponse>> getHistory() {
        log.info("Received request to fetch transaction history");
        List<PaymentResponse> history = paymentService.getTransactionHistory();
        return ResponseEntity.ok(history);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        log.error("Error processing request", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
    }
}

// Made with Bob
