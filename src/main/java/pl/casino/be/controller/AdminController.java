package pl.casino.be.controller;

import com.google.firebase.auth.FirebaseAuthException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import pl.casino.be.dto.GameHistoryDto;
import pl.casino.be.dto.TransactionDto;
import pl.casino.be.service.AdminService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin Panel", description = "Endpoints for casino management (ADMIN role required)")
@SecurityRequirement(name = "bearerAuth")
@Secured("ROLE_ADMIN")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/users/{uid}/promote")
    @Operation(summary = "Grant administrator privileges",
            description = "Sets the custom claim 'role' to 'ADMIN' for the user with the given UID.")
    public ResponseEntity<?> promoteUserToAdmin(@PathVariable String uid) {
        try {
            adminService.setUserRoleToAdmin(uid);
            return ResponseEntity.ok(Map.of("message", "User " + uid + " has been promoted to ADMIN."));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error promoting user: " + e.getMessage()));
        }
    }

    @GetMapping("/transactions")
    @Operation(summary = "Get all transactions history",
            description = "Returns a list of all financial transactions in the system.")
    public ResponseEntity<List<TransactionDto>> getAllTransactions() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(adminService.getAllTransactions());
    }

    @GetMapping("/games")
    @Operation(summary = "Get all games history",
            description = "Returns a list of all games played in the system.")
    public ResponseEntity<List<GameHistoryDto>> getAllGameHistories() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(adminService.getAllGameHistories());
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get casino statistics",
            description = "Returns key statistics about the casino's operation.")
    public ResponseEntity<Map<String, Object>> getStatistics() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(adminService.getCasinoStatistics());
    }
}
