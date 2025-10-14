package pl.casino.be.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import pl.casino.be.dto.BetRequest;
import pl.casino.be.service.CosmicCashoutService;

import java.security.Principal;

@Controller
public class CosmicCashoutController {

    private final CosmicCashoutService cashoutService;

    public CosmicCashoutController(CosmicCashoutService cashoutService) {
        this.cashoutService = cashoutService;
    }

    @MessageMapping("/cosmic-cashout/bet")
    public void placeBet(BetRequest betRequest, Principal principal) {
        if (principal != null) {
            String uid = principal.getName();
            cashoutService.placeBet(uid, betRequest.displayName(), betRequest.amount());
        }
    }

    @MessageMapping("/cosmic-cashout/cashout")
    public void cashOut(Principal principal) {
        if (principal != null) {
            String uid = principal.getName();
            cashoutService.cashOut(uid);
        }
    }
}