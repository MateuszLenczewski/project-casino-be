package pl.casino.be.dto;

import java.math.BigDecimal;
import java.util.List;

public record CosmicCashoutStateDto(
        String gameState,
        BigDecimal currentMultiplier,
        List<PlayerStateDto> playersInRound,
        List<BigDecimal> crashHistory
) {}