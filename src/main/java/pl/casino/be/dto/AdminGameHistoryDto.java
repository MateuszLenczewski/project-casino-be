package pl.casino.be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.casino.be.model.GameType;
import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminGameHistoryDto {
    private String username;
    private String userId;
    private GameType gameType;
    private BigDecimal betAmount;
    private BigDecimal winAmount;
    private String result;
    private Date timestamp;
}