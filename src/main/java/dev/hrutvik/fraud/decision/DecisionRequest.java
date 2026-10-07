package dev.hrutvik.fraud.decision;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record DecisionRequest(
        @NotBlank @Size(max=100) String transactionId,
        @NotBlank @Size(max=100) String accountId,
        @NotBlank @Size(max=100) String deviceId,
        @NotBlank @Size(max=45) String ipAddress,
        @NotNull @DecimalMin("0.01") @Digits(integer=15, fraction=4) BigDecimal amount,
        @NotBlank @Pattern(regexp="[A-Za-z]{3}") String currency,
        @NotBlank @Size(max=2) String billingCountry,
        @NotBlank @Size(max=2) String ipCountry,
        boolean cardPresent,
        @Min(0) @Max(1000) int accountAgeDays) {}
