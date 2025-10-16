package pl.casino.be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.casino.be.model.TransactionType;
import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminTransactionDto {
    private String username;
    private String userId;
    private TransactionType type;
    private BigDecimal amount;
    private Date timestamp;
}