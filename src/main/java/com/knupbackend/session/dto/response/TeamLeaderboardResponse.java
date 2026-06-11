package com.knupbackend.session.dto.response;

import com.knupbackend.session.domain.Participant;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public record TeamLeaderboardResponse(List<TeamLeaderboardEntry> entries) {

    public static TeamLeaderboardResponse of(List<Participant> participants) {
        // 과거 데이터 등으로 teamName 이 비어 있어도 NPE 없이 묶이도록 방어
        Map<String, List<Participant>> byTeam = participants.stream()
                .collect(Collectors.groupingBy(p ->
                        (p.getTeamName() == null || p.getTeamName().isBlank()) ? "미지정" : p.getTeamName()));

        AtomicInteger rank = new AtomicInteger(1);
        List<TeamLeaderboardEntry> entries = byTeam.entrySet().stream()
                .sorted(Comparator.comparingInt(
                        (Map.Entry<String, List<Participant>> e) ->
                                e.getValue().stream().mapToInt(Participant::getScore).sum()
                ).reversed())
                .map(e -> TeamLeaderboardEntry.of(rank.getAndIncrement(), e.getKey(), e.getValue()))
                .toList();

        return new TeamLeaderboardResponse(entries);
    }
}
