package pl.casino.be.dto;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.casino.be.model.GameType;
import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
public class GameHistoryDto {
    @DocumentId
    private String id;
    private String userId;
    private GameType gameType;
    private BigDecimal betAmount;
    private BigDecimal winAmount;
    private String result;
    private Date timestamp;
}