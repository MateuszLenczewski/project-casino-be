package pl.casino.be.model;

import lombok.Data;

import java.math.BigDecimal;

@Data // Lombok - generuje gettery, settery, etc.
public class User {
    private String uid; // Identyfikator z Firebase Auth
    private String email;
    private String displayName;
    private BigDecimal balance;
}