package pl.casino.be.service;

import com.google.cloud.firestore.Firestore;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.casino.be.dto.CosmicCashoutStateDto;
import pl.casino.be.dto.PlayerStateDto;
import pl.casino.be.model.GameHistory;
import pl.casino.be.model.GameType;
import pl.casino.be.model.PlayerStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CosmicCashoutService {

    private final SimpMessagingTemplate messagingTemplate;
    private final WalletService walletService;
    private final Firestore firestore;
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private volatile GameState gameState = GameState.WAITING;
    private volatile BigDecimal currentMultiplier = BigDecimal.ONE;

    private final Map<String, PlayerStateDto> playersInRound = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final List<BigDecimal> crashHistory = new CopyOnWriteArrayList<>();


    private final Object gameStateLock = new Object();
    private static final String GAME_HISTORY_COLLECTION = "game_history";

    public CosmicCashoutService(SimpMessagingTemplate messagingTemplate, WalletService walletService, Firestore firestore) {
        this.messagingTemplate = messagingTemplate;
        this.walletService = walletService;
        this.firestore = firestore;
    }

    public CosmicCashoutStateDto getCurrentGameState() {
        // Returns the current game state, including the list of players and crash history.
        var playerList = playersInRound.values().stream()
                .sorted(Comparator.comparing(PlayerStateDto::getBetAmount).reversed())
                .collect(Collectors.toList());
        return new CosmicCashoutStateDto(gameState.name(), currentMultiplier, playerList, crashHistory);
    }

    /**
     * Broadcasts the current list of players to all subscribers.
     */
    private void broadcastPlayerList() {
        var playerList = playersInRound.values().stream()
                .sorted(Comparator.comparing(PlayerStateDto::getBetAmount).reversed())
                .collect(Collectors.toList());
        messagingTemplate.convertAndSend("/topic/cashout/players", playerList);
    }

    @Scheduled(fixedRate = 15000)
    public void gameLoop() {
        startNewRound();
    }

    private void startNewRound() {
        synchronized (gameStateLock) {
            if (gameState == GameState.WAITING) {
                gameState = GameState.RUNNING;
                currentMultiplier = BigDecimal.ONE;
                log.info("Starting new Cosmic Cashout round with {} players.", playersInRound.size());
                messagingTemplate.convertAndSend("/topic/cashout/state", "RUNNING");

                double crashPoint = 1.0 + random.nextDouble() * 2.0;

                virtualThreadExecutor.submit(() -> {
                    try {
                        playersInRound.values().forEach(p -> p.setStatus(PlayerStatus.IN_GAME));
                        broadcastPlayerList();

                        while (currentMultiplier.doubleValue() < crashPoint && gameState == GameState.RUNNING) {
                            currentMultiplier = currentMultiplier.add(new BigDecimal("0.01"));
                            messagingTemplate.convertAndSend("/topic/cashout/multiplier", currentMultiplier);
                            Thread.sleep(100);
                        }
                        if (gameState == GameState.RUNNING) {
                            crash(crashPoint);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        }
    }

    private void crash(double crashPoint) {
        gameState = GameState.CRASHED;
        BigDecimal finalMultiplier = BigDecimal.valueOf(crashPoint).setScale(2, RoundingMode.DOWN);
        String formattedCrashPoint = finalMultiplier.toPlainString();

        log.info("CRASH! Multiplier stopped at {}", formattedCrashPoint);
        messagingTemplate.convertAndSend("/topic/cashout/crash", formattedCrashPoint);

        crashHistory.add(0, finalMultiplier); 
        if (crashHistory.size() > 10) {
            crashHistory.remove(crashHistory.size() - 1); 
        }
        messagingTemplate.convertAndSend("/topic/cashout/history", crashHistory);



        playersInRound.values().stream()
                .filter(p -> p.getStatus() == PlayerStatus.IN_GAME)
                .forEach(player -> {
                    // We need to find the UID by username, which is a flaw.
                    // For now, we assume username is unique or we find the first match.
                    playersInRound.entrySet().stream()
                            .filter(entry -> entry.getValue().equals(player))
                            .findFirst()
                            .ifPresent(entry -> {
                                String result = "Loss. Crash at " + formattedCrashPoint + "x";
                                saveGameHistory(entry.getKey(), player.getBetAmount(), BigDecimal.ZERO, result);
                            });
                });

        playersInRound.clear();
        broadcastPlayerList();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            gameState = GameState.WAITING;
            messagingTemplate.convertAndSend("/topic/cashout/state", "WAITING");
        }
    }

    public void placeBet(String uid, String displayName, BigDecimal amount) {
        synchronized (gameStateLock) {
            if (gameState == GameState.WAITING && !playersInRound.containsKey(uid)) {
                walletService.placeBet(uid, amount);
                PlayerStateDto playerState = new PlayerStateDto(displayName, amount, PlayerStatus.IN_GAME, null);
                playersInRound.put(uid, playerState);
                log.info("User {} ({}) placed a bet of {}", uid, displayName, amount);
                broadcastPlayerList();
            } else {
                log.warn("Bet rejected for user {}. State: {} or already in round.", uid, gameState);
            }
        }
    }

    public void cashOut(String uid) {
        BigDecimal cashoutMultiplier = currentMultiplier.setScale(2, RoundingMode.DOWN);
        if (gameState == GameState.RUNNING && playersInRound.containsKey(uid)) {
            PlayerStateDto playerState = playersInRound.get(uid);

            // Prevent cashing out twice
            if (playerState.getStatus() == PlayerStatus.CASHED_OUT) return;

            BigDecimal betAmount = playerState.getBetAmount();
            BigDecimal winnings = betAmount.multiply(cashoutMultiplier).setScale(2, RoundingMode.DOWN);

            walletService.processWin(uid, winnings);

            // Update player state
            playerState.setStatus(PlayerStatus.CASHED_OUT);
            playerState.setCashOutMultiplier(cashoutMultiplier.setScale(2, RoundingMode.DOWN));

            broadcastPlayerList();

            String result = "Win. Cashed out at " + playerState.getCashOutMultiplier() + "x";
            saveGameHistory(uid, betAmount, winnings, result);

            log.info("User {} cashed out at {}x, winning {}", uid, playerState.getCashOutMultiplier(), winnings);
        }
    }

    private void saveGameHistory(String uid, BigDecimal bet, BigDecimal win, String result) {
        GameHistory history = new GameHistory();
        history.setUserId(uid);
        history.setGameType(GameType.COSMIC_CASHOUT);
        history.setBetAmount(bet);
        history.setWinAmount(win);
        history.setResult(result);
        history.setTimestamp(new Date());
        firestore.collection(GAME_HISTORY_COLLECTION).document().set(history);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down virtual thread executor.");
        virtualThreadExecutor.shutdown();
    }

    private enum GameState { WAITING, RUNNING, CRASHED }
}