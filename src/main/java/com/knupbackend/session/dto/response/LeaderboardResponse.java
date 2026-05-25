package com.knupbackend.session.dto.response;

import com.knupbackend.session.domain.GameSession;
import com.knupbackend.session.domain.Participant;
import lombok.Getter;

import java.util.List;
import java.util.stream.IntStream;

@Getter
public class LeaderboardResponse {
    private final String sessionId;
    private final int currentQuestion;
    private final List<LeaderboardEntry> entries;

    public LeaderboardResponse(GameSession session, List<Participant> sorted, int top) {
        this.sessionId       = session.getSessionId();
        this.currentQuestion = session.getCurrentQuestionIndex();
        this.entries = IntStream.range(0, Math.min(top, sorted.size()))
                .mapToObj(i -> new LeaderboardEntry(i + 1, sorted.get(i)))
                .toList();
    }
}
