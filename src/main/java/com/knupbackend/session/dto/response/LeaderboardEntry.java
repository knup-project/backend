package com.knupbackend.session.dto.response;

import com.knupbackend.session.domain.Participant;

public record LeaderboardEntry(
        int rank,
        String participantId,
        String nickname,
        int totalPoints,
        int correctCount,
        double averageResponseTimeSec
) {
    public static LeaderboardEntry of(int rank, Participant p) {
        return new LeaderboardEntry(
                rank,
                p.getParticipantId(),
                p.getNickname(),
                p.getScore(),
                p.getCorrectCount(),
                Math.round(p.averageResponseTimeSec() * 10.0) / 10.0
        );
    }
}
