package pl.casino.be.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.casino.be.dto.PublicGameHistoryDto;
import pl.casino.be.service.DashboardService;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Public endpoints for dashboard information")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/big-wins")
    public ResponseEntity<List<PublicGameHistoryDto>> getRecentBigWins() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(dashboardService.getRecentBigWins());
    }
}