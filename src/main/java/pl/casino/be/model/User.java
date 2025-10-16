package pl.casino.be.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class User {
    private String uid;
    private String email;
    private String displayName;
    private BigDecimal balance;
}