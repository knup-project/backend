package com.knupbackend.session.dto.response;

import com.knupbackend.session.domain.GameSession;
import com.knupbackend.session.domain.Participant;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class SessionStatsResponse {
    private final int totalParticipants;
    private final int currentQuestionIndex;
    private final double overallAccuracy;
    private final Map<String, Integer> answerDistribution;
    private final int answeredCount;
    private final double averageResponseTimeSec;

    public SessionStatsResponse(GameSession session,
                                List<Participant> participants,
                                Map<String, Integer> answerDistribution) {
        this.totalParticipants    = participants.size();
        this.currentQuestionIndex = session.getCurrentQuestionIndex();

        int totalAnswered = participants.stream().mapToInt(Participant::getAnswerCount).sum();
        int totalCorrect  = participants.stream().mapToInt(Participant::getCorrectCount).sum();
        double totalTime  = participants.stream().mapToDouble(Participant::getTotalResponseTimeSec).sum();

        this.answeredCount          = totalAnswered;
        this.overallAccuracy        = totalAnswered == 0 ? 0.0
                : Math.round((double) totalCorrect / totalAnswered * 100.0) / 100.0;
        this.averageResponseTimeSec = totalAnswered == 0 ? 0.0
                : Math.round(totalTime / totalAnswered * 10.0) / 10.0;
        this.answerDistribution     = answerDistribution;
    }
}
