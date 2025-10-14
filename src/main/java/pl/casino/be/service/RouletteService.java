package pl.casino.be.service;


import com.google.cloud.firestore.Firestore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.casino.be.dto.BetRequest;
import pl.casino.be.model.GameHistory;
import pl.casino.be.model.GameType;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Set;

@Slf4j
@Service
public class RouletteService {

    private final WalletService walletService;
    private final Firestore firestore;
    private final SecureRandom random = new SecureRandom();

    private static final String GAME_HISTORY_COLLECTION = "game_history";
    private static final Set<Integer> RED_NUMBERS = Set.of(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36);

    public RouletteService(WalletService walletService, Firestore firestore) {
        this.walletService = walletService;
        this.firestore = firestore;
    }

    public GameHistory play(String uid, BetRequest bet) {
        // 1. Place the bet (deduct from wallet)
        walletService.placeBet(uid, bet.amount());

        // 2. Spin the roulette
        int winningNumber = random.nextInt(37); // 0-36

        // 3. Calculate winnings
        BigDecimal winnings = calculateWinnings(bet, winningNumber);

        // 4. Process winnings (add to wallet if any)
        walletService.processWin(uid, winnings);

        // 5. Log game history
        String resultDescription = winningNumber == 0 ? "0" : String.valueOf(winningNumber);
        GameHistory gameHistory = createGameHistory(uid, bet.amount(), winnings, resultDescription);
        saveGameHistory(gameHistory);

        log.info("Roulette played by {}. Bet on {} {}. Winning number: {}. Won: {}", uid, bet.betType(), bet.betValue(), winningNumber, winnings);

        return gameHistory;
    }

    private BigDecimal calculateWinnings(BetRequest bet, int winningNumber) {
        switch (bet.betType().toLowerCase()) {
            case "number":
                int betNumber = Integer.parseInt(bet.betValue());
                return (winningNumber == betNumber) ? bet.amount().multiply(BigDecimal.valueOf(36)) : BigDecimal.ZERO;
            case "color":
                if (winningNumber == 0) return BigDecimal.ZERO;
                boolean isRed = RED_NUMBERS.contains(winningNumber);
                if ((bet.betValue().equalsIgnoreCase("red") && isRed) || (bet.betValue().equalsIgnoreCase("black") && !isRed)) {
                    return bet.amount().multiply(BigDecimal.valueOf(2));
                }
                return BigDecimal.ZERO;
            // TODO add more bet types like odd/even, high/low, dozens, etc.
            default:
                return BigDecimal.ZERO;
        }
    }

    private GameHistory createGameHistory(String uid, BigDecimal betAmount, BigDecimal winAmount, String result) {
        GameHistory history = new GameHistory();
        history.setUserId(uid);
        history.setGameType(GameType.ROULETTE);
        history.setBetAmount(betAmount);
        history.setWinAmount(winAmount);
        history.setResult(result);
        history.setTimestamp(new Date());
        return history;
    }

    private void saveGameHistory(GameHistory history) {
        firestore.collection(GAME_HISTORY_COLLECTION).document().set(history);
    }
}