package pl.casino.be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.casino.be.model.PlayerStatus;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStateDto {
    private String username;
    private BigDecimal betAmount;
    private PlayerStatus status;
    private BigDecimal cashOutMultiplier; // Can be null if status is IN_GAME
}