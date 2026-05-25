package com.knupbackend.session.dto.response;

import com.knupbackend.session.domain.GameSession;
import com.knupbackend.session.domain.Participant;

import java.util.List;
import java.util.stream.IntStream;

public record LeaderboardResponse(
        String sessionId,
        int currentQuestion,
        List<LeaderboardEntry> entries
) {
    public static LeaderboardResponse of(GameSession session, List<Participant> sorted, int top) {
        return new LeaderboardResponse(
                session.getSessionId(),
                session.getCurrentQuestionIndex(),
                IntStream.range(0, Math.min(top, sorted.size()))
                        .mapToObj(i -> LeaderboardEntry.of(i + 1, sorted.get(i)))
                        .toList()
        );
    }
}
