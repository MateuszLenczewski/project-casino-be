package pl.casino.be.service;

import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.casino.be.dto.UserProfileDto;
import pl.casino.be.model.GameHistory;
import pl.casino.be.model.User;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class UserService {

    private final Firestore firestore;
    private static final String USERS_COLLECTION = "users";
    private static final String GAME_HISTORY_COLLECTION = "game_history";

    public UserService(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Checks if a user exists in Firestore by their UID.
     * If not, creates a new user document with initial data.
     * @param token FirebaseToken containing user info.
     */
    public void findOrCreateUser(FirebaseToken token) {
        DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(token.getUid());
        try {
            if (!userRef.get().get().exists()) {
                log.info("Creating a new user profile for UID: {}", token.getUid());
                User newUser = new User();
                newUser.setUid(token.getUid());
                newUser.setEmail(token.getEmail());
                newUser.setDisplayName(token.getName());
                newUser.setBalance(BigDecimal.ZERO);

                userRef.set(newUser).get();
                log.info("Successfully created new user: {}", token.getEmail());
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while trying to find or create user with UID: {}", token.getUid(), e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Gets the full profile of a user, including basic info, last 20 transactions, and last 20 games.
     * @param uid User ID to retrieve the profile for.
     * @return DTO containing user profile data.
     */
    public UserProfileDto getUserProfile(String uid) {
        DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(uid);
        try {
            DocumentSnapshot userDoc = userRef.get().get();
            if (userDoc.exists()) {
                User user = userDoc.toObject(User.class);
                assert user != null;
                return new UserProfileDto(user.getUid(), user.getEmail(), user.getDisplayName(), user.getBalance());
            }
            throw new RuntimeException(MessageFormat.format("User not found with UID: {0}", uid));
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error retrieving profile for UID: {}", uid, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to retrieve user profile.", e);
        }
    }

    public List<GameHistory> getGameHistoryForUser(String uid) throws ExecutionException, InterruptedException {
        return firestore.collection(GAME_HISTORY_COLLECTION)
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().get().toObjects(GameHistory.class);
    }

}