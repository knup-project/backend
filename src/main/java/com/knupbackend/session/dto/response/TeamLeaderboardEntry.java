package com.knupbackend.session.dto.response;

import com.knupbackend.session.domain.Participant;

import java.util.List;
import java.util.UUID;

public record TeamLeaderboardEntry(
        int rank,
        String teamId,
        String teamName,
        int totalPoints,
        int memberCount,
        List<LeaderboardEntry> members
) {
    public static TeamLeaderboardEntry of(int rank, String teamName, List<Participant> members) {
        return new TeamLeaderboardEntry(
                rank,
                UUID.nameUUIDFromBytes(teamName.getBytes()).toString(),
                teamName,
                members.stream().mapToInt(Participant::getScore).sum(),
                members.size(),
                members.stream()
                        .sorted((a, b) -> b.getScore() - a.getScore())
                        .map(p -> LeaderboardEntry.of(0, p))
                        .toList()
        );
    }
}
