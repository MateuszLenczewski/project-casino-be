package pl.casino.be.dto;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.casino.be.model.TransactionType;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
public class TransactionDto {
    @DocumentId
    private String id;
    private String userId;
    private TransactionType type;
    private BigDecimal amount;
    private Date timestamp;
}