package com.knupbackend.session.dto.response;

import com.knupbackend.session.domain.Participant;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Getter
public class TeamLeaderboardResponse {
    private final List<TeamLeaderboardEntry> entries;

    public TeamLeaderboardResponse(List<Participant> participants) {
        Map<String, List<Participant>> byTeam = participants.stream()
                .collect(Collectors.groupingBy(Participant::getTeamName));

        AtomicInteger rank = new AtomicInteger(1);
        this.entries = byTeam.entrySet().stream()
                .sorted(Comparator.comparingInt(
                        (Map.Entry<String, List<Participant>> e) ->
                                e.getValue().stream().mapToInt(Participant::getScore).sum()
                ).reversed())
                .map(e -> new TeamLeaderboardEntry(rank.getAndIncrement(), e.getKey(), e.getValue()))
                .toList();
    }
}
