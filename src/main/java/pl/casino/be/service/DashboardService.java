package pl.casino.be.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.casino.be.dto.PublicGameHistoryDto;
import pl.casino.be.model.GameHistory;
import pl.casino.be.service.utils.UsernameGenerator; // Import the generator

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final Firestore firestore;
    // No longer need UserService

    public List<PublicGameHistoryDto> getRecentBigWins() throws ExecutionException, InterruptedException {
        // Fetch the last 20 games where the winAmount was greater than zero
        List<GameHistory> games = firestore.collection("game_history")
                .whereGreaterThan("winAmount", "0") // Filter for wins only
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .get().get().toObjects(GameHistory.class);

        // Map to the DTO, generating a fake username for each entry
        return games.stream().map(game -> new PublicGameHistoryDto(
                UsernameGenerator.generateFakeUsername(), // Use the generator
                game.getGameType(),
                game.getBetAmount(),
                game.getWinAmount(),
                game.getResult(),
                game.getTimestamp()
        )).collect(Collectors.toList());
    }
}