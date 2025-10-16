package pl.casino.be.service;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.casino.be.exception.InsufficientFundsException;
import pl.casino.be.model.Transaction;
import pl.casino.be.model.TransactionType;
import pl.casino.be.model.User;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class WalletService {

    private final Firestore firestore;
    private static final String USERS_COLLECTION = "users";
    private static final String TRANSACTIONS_COLLECTION = "transactions";

    public WalletService(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Deposits funds into a user's account in a transaction.
     * @param uid IIdentifier of the user.
     * @param amount Amount to deposit.
     */
    public void deposit(String uid, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        updateBalance(uid, amount, TransactionType.DEPOSIT);
    }

    /**
     * Withdraws funds from a user's account in a transaction.
     * @param uid Identifier of the user.
     * @param amount amount to withdraw.
     */
    public void withdraw(String uid, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        updateBalance(uid, amount.negate(), TransactionType.WITHDRAWAL);
    }

    /**
     * Places a bet by deducting the bet amount from the user's balance in a transaction.
     * @param uid Identifier of the user.
     * @param amount Bet amount (positive).
     */
    public void placeBet(String uid, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Bet amount must be positive.");
        }
        updateBalance(uid, amount.negate(), TransactionType.BET);
    }

    /**
     * Processes a win by adding the winning amount to the user's balance in a transaction.
     * @param uid Identifier of the user.
     * @param amount Winning amount (positive).
     */
    public void processWin(String uid, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            updateBalance(uid, amount, TransactionType.WIN);
        }
    }

    /**
     * Updates the user's balance and logs the transaction atomically.
         * @param uid Identifier of the user.
     * @param amount Amount to add (positive) or subtract (negative).
     * @param type Transaction type.
     */
    private void updateBalance(String uid, BigDecimal amount, TransactionType type) {
        final DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(uid);
        final DocumentReference transactionRef = firestore.collection(TRANSACTIONS_COLLECTION).document(UUID.randomUUID().toString());

        try {
            firestore.runTransaction(tx -> {
                DocumentSnapshot userDoc = tx.get(userRef).get();
                if (!userDoc.exists()) {
                    throw new IllegalStateException(MessageFormat.format("User not found: {0}", uid));
                }
                User user = userDoc.toObject(User.class);
                assert user != null;
                BigDecimal currentBalance = user.getBalance();
                BigDecimal newBalance = currentBalance.add(amount);

                if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                    throw new InsufficientFundsException(MessageFormat.format("Insufficient funds for user: {0}", uid));
                }

                // Update user balance
                tx.update(userRef, "balance", newBalance);

                // Create transaction record
                Transaction newTransaction = new Transaction();
                newTransaction.setUserId(uid);
                newTransaction.setAmount(amount.abs());
                newTransaction.setType(type);
                newTransaction.setTimestamp(new Date());
                tx.set(transactionRef, newTransaction);

                return null;
            }).get();
            log.info("Transaction successful for user {}, type: {}, amount: {}", uid, type, amount);
        } catch (InterruptedException | ExecutionException e) {
            // Rethrow custom exceptions
            if (e.getCause() instanceof InsufficientFundsException) {
                throw (InsufficientFundsException) e.getCause();
            }
            log.error("Transaction failed for user {}: {}", uid, e.getMessage());
            Thread.currentThread().interrupt();
            throw new RuntimeException("Database transaction failed.", e);
        }
    }
}