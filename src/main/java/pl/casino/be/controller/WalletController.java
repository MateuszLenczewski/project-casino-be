package pl.casino.be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.casino.be.service.WalletService;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wallet")
@Tag(name = "Wallet", description = "Financial operations such as deposits and withdrawals")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/deposit")
    @Operation(summary = "Deposit funds into the account",
            description = "Deposits a specified amount into the user's balance.")
    public ResponseEntity<Map<String, String>> deposit(Principal principal, @RequestBody Map<String, BigDecimal> payload) {
        String uid = principal.getName();
        BigDecimal amount = payload.get("amount");
        walletService.deposit(uid, amount);
        return ResponseEntity.ok(Map.of("message", "Deposit successful."));
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw funds from the account",
            description = "Subtracts a specified amount from the user's balance if sufficient funds are available.")
    public ResponseEntity<Map<String, String>> withdraw(Principal principal, @RequestBody Map<String, BigDecimal> payload) {
        String uid = principal.getName();
        BigDecimal amount = payload.get("amount");
        walletService.withdraw(uid, amount);
        return ResponseEntity.ok(Map.of("message", "Withdrawal successful."));
    }
}
