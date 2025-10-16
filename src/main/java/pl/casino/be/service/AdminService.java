package pl.casino.be.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.casino.be.dto.*;
import pl.casino.be.model.GameHistory;
import pl.casino.be.model.Transaction;
import pl.casino.be.model.User;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminService {

    private final Firestore firestore;
    private final FirebaseAuth firebaseAuth;

    public AdminService(Firestore firestore, FirebaseAuth firebaseAuth) {
        this.firestore = firestore;
        this.firebaseAuth = firebaseAuth;
    }

    private Map<String, String> getUserMap() throws ExecutionException, InterruptedException {
        return firestore.collection("users").get().get().toObjects(User.class).stream()
                .collect(Collectors.toMap(User::getUid, User::getDisplayName, (existing, _) -> existing));
    }

    /**
     * Downloads all transactions from the system, sorted from newest to oldest.
     * @return List of transaction DTOs.
     */
    public List<AdminTransactionDto> getAllTransactions() throws ExecutionException, InterruptedException {
        Map<String, String> userMap = getUserMap(); // Get the guest list
        List<Transaction> transactions = firestore.collection("transactions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50) // Limit for performance
                .get().get().toObjects(Transaction.class);

        // Map the raw data to the new DTO, adding the username
        return transactions.stream().map(tx -> new AdminTransactionDto(
                userMap.getOrDefault(tx.getUserId(), "Unknown User"), // Look up the name
                tx.getUserId(),
                tx.getType(),
                tx.getAmount(),
                tx.getTimestamp()
        )).collect(Collectors.toList());
    }

    /**
     * Downloads all game histories from the system, sorted from newest to oldest.
     * @return List of game history DTOs.
     */
    public List<AdminGameHistoryDto> getAllGameHistories() throws ExecutionException, InterruptedException {
        Map<String, String> userMap = getUserMap(); // Get the guest list
        List<GameHistory> games = firestore.collection("game_history")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50) // Limit for performance
                .get().get().toObjects(GameHistory.class);

        // Map the raw data to the new DTO, adding the username
        return games.stream().map(game -> new AdminGameHistoryDto(
                userMap.getOrDefault(game.getUserId(), "Unknown User"), // Look up the name
                game.getUserId(),
                game.getGameType(),
                game.getBetAmount(),
                game.getWinAmount(),
                game.getResult(),
                game.getTimestamp()
        )).collect(Collectors.toList());
    }
    /**
     * Gives a user admin privileges by setting a custom claim in Firebase Authentication.
     * @param uid User ID to promote to admin.
     * @throws FirebaseAuthException if there's an error setting custom claims.
     */
    public void setUserRoleToAdmin(String uid) throws FirebaseAuthException {
        Map<String, Object> claims = Map.of("role", "ADMIN");
        firebaseAuth.setCustomUserClaims(uid, claims);
        log.info("Promoted user {} to ADMIN", uid);
    }

    /**
     * Downloads basic statistics about the casino system.
     * @return Map containing totalUsers, totalTransactions, and totalGamesPlayed.
     */
    public Map<String, Object> getCasinoStatistics() throws ExecutionException, InterruptedException {
        long totalUsers = firestore.collection("users").get().get().size();
        long totalTransactions = firestore.collection("transactions").get().get().size();
        long totalGamesPlayed = firestore.collection("game_history").get().get().size();

        return Map.of(
                "totalUsers", totalUsers,
                "totalTransactions", totalTransactions,
                "totalGamesPlayed", totalGamesPlayed
        );
    }

    public List<UserProfileDto> getAllUsers() throws ExecutionException, InterruptedException {
        return firestore.collection("users")
                .get().get().toObjects(UserProfileDto.class);
    }
}