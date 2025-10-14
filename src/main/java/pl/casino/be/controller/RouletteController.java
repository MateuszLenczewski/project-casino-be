package pl.casino.be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.casino.be.dto.BetRequest;
import pl.casino.be.model.GameHistory;
import pl.casino.be.service.RouletteService;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/games/roulette")
@Tag(name = "Games - Roulette", description = "Endpoint for the Roulette game")
@SecurityRequirement(name = "bearerAuth")
public class RouletteController {

    private final RouletteService rouletteService;

    public RouletteController(RouletteService rouletteService) {
        this.rouletteService = rouletteService;
    }

    @PostMapping("/play")
    @Operation(summary = "Play Roulette",
            description = "Accepts a bet, performs a spin, updates the balance, and returns the game result.")
    public ResponseEntity<GameHistory> play(Principal principal, @RequestBody BetRequest betRequest) {
        String uid = principal.getName();
        GameHistory result = rouletteService.play(uid, betRequest);
        return ResponseEntity.ok(result);
    }
}