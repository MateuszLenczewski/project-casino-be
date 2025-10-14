package pl.casino.be.dto;

import java.math.BigDecimal;

public record BetRequest(
        BigDecimal amount,
        String betType,
        String betValue,
        String displayName
) {}