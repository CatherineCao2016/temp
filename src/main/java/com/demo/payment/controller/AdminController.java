package com.demo.payment.controller;

import com.demo.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final PaymentService paymentService;

    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, String>> clearCache() {
        log.info("Received request to clear cache");
        paymentService.clearCache();
        return ResponseEntity.ok(Map.of("message", "Cache cleared successfully"));
    }
}

// Made with Bob
