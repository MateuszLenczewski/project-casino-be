package pl.casino.be.model;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.Exclude;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Transaction {
    @DocumentId
    @Exclude
    private String id;
    private String userId;
    private TransactionType type; // DEPOSIT, WITHDRAWAL, BET, WIN
    private BigDecimal amount;
    private Date timestamp;
}


