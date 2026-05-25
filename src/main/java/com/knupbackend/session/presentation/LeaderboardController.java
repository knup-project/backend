package com.knupbackend.session.presentation;

import com.knupbackend.global.auth.LoginUser;
import com.knupbackend.session.dto.response.LeaderboardResponse;
import com.knupbackend.session.dto.response.SessionStatsResponse;
import com.knupbackend.session.dto.response.TeamLeaderboardResponse;
import com.knupbackend.session.service.LeaderboardService;
import com.knupbackend.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sessions/{sessionId}")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    /** 5.1 개인 리더보드 조회 (Auth 불필요) */
    @GetMapping("/leaderboard")
    public ResponseEntity<LeaderboardResponse> getLeaderboard(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "10") int top) {
        return ResponseEntity.ok(leaderboardService.getLeaderboard(sessionId, top));
    }

    /** 5.2 팀 리더보드 조회 (Auth 불필요) */
    @GetMapping("/leaderboard/teams")
    public ResponseEntity<TeamLeaderboardResponse> getTeamLeaderboard(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(leaderboardService.getTeamLeaderboard(sessionId));
    }

    /** 5.3 실시간 통계 - 호스트 전용 */
    @GetMapping("/stats")
    public ResponseEntity<SessionStatsResponse> getStats(
            @PathVariable String sessionId,
            @LoginUser User user) {
        return ResponseEntity.ok(leaderboardService.getStats(sessionId, user));
    }
}
