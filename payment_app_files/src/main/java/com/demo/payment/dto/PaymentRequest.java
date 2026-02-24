package com.demo.payment.dto;

import javax.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "\\d{13,19}", message = "Invalid card number")
    private String cardNumber;

    @NotBlank(message = "Card expiry is required")
    @Pattern(regexp = "(0[1-9]|1[0-2])/\\d{2}", message = "Invalid expiry format (MM/YY)")
    private String cardExpiry;

    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "\\d{3,4}", message = "Invalid CVV")
    private String cvv;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Amount too large")
    private BigDecimal amount;
}

// Made with Bob
