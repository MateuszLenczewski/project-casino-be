package pl.casino.be.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.casino.be.dto.CosmicCashoutStateDto;
import pl.casino.be.service.CosmicCashoutService;

@RestController
@RequestMapping("/api/v1/games/cosmic-cashout")
@Tag(name = "Games - Cosmic Cashout", description = "Endpoint for the Cosmic Cashout game")
public class CosmicCashoutRestController {

    private final CosmicCashoutService cosmicCashoutService;

    public CosmicCashoutRestController(CosmicCashoutService cosmicCashoutService) {
        this.cosmicCashoutService = cosmicCashoutService;
    }

    @GetMapping("/state")
    public CosmicCashoutStateDto getGameState() {
        return cosmicCashoutService.getCurrentGameState();
    }
}