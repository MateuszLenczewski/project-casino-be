package pl.casino.be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.casino.be.dto.UserProfileDto;
import pl.casino.be.model.GameHistory;
import pl.casino.be.service.UserService;

import java.security.Principal;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "User Profile", description = "Operations related to the user profile")
@SecurityRequirement(name = "bearerAuth") // Requires authentication for all methods in this class
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get the logged-in user's profile",
            description = "Returns the full user profile, including balance, game history, and transaction history.")
    public ResponseEntity<UserProfileDto> getUserProfile(Principal principal) {
        // Principal.getName() returns the user's UID set in FirebaseTokenFilter
        String uid = principal.getName();
        UserProfileDto userProfile = userService.getUserProfile(uid);
        return ResponseEntity.ok(userProfile);
    }

    @GetMapping("/games")
    @Operation(summary = "Get the logged-in user's game history",
            description = "Returns the full game history for the authenticated user.")
    public ResponseEntity<List<GameHistory>> getMyGameHistory(Principal principal) throws ExecutionException, InterruptedException {
        String uid = principal.getName();
        return ResponseEntity.ok(userService.getGameHistoryForUser(uid));
    }
}