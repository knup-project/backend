package com.knupbackend.session.dto.response;

import com.knupbackend.session.domain.Participant;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class TeamLeaderboardEntry {
    private final int rank;
    private final String teamId;
    private final String teamName;
    private final int totalScore;
    private final int memberCount;
    private final List<LeaderboardEntry> members;

    public TeamLeaderboardEntry(int rank, String teamName, List<Participant> members) {
        this.rank        = rank;
        this.teamId      = UUID.nameUUIDFromBytes(teamName.getBytes()).toString();
        this.teamName    = teamName;
        this.totalScore  = members.stream().mapToInt(Participant::getScore).sum();
        this.memberCount = members.size();
        this.members     = members.stream()
                .sorted((a, b) -> b.getScore() - a.getScore())
                .map(p -> new LeaderboardEntry(0, p))
                .toList();
    }
}
