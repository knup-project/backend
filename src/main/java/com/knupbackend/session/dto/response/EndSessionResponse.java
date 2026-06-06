package com.knupbackend.session.dto.response;

import java.util.List;

public record EndSessionResponse(
        List<LeaderboardEntry> finalLeaderboard,
        int totalParticipants,
        long sessionDurationSec
) {}
