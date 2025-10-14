package pl.casino.be.model;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.Exclude;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class GameHistory {
    @DocumentId
    @Exclude
    private String id;
    private String userId;
    private GameType gameType; // ROULETTE, COSMIC_CASHOUT
    private BigDecimal betAmount;
    private BigDecimal winAmount;
    private String result; // e.g. "32" for roulette or "2.5" for cosmic cashout
    private Date timestamp;
}