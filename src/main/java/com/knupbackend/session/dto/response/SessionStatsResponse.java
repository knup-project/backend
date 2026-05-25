package com.knupbackend.session.dto.response;

import com.knupbackend.session.domain.GameSession;
import com.knupbackend.session.domain.Participant;

import java.util.List;
import java.util.Map;

public record SessionStatsResponse(
        int totalParticipants,
        int currentQuestionIndex,
        double overallAccuracy,
        Map<String, Integer> answerDistribution,
        int answeredCount,
        double averageResponseTimeSec
) {
    public static SessionStatsResponse of(GameSession session,
                                          List<Participant> participants,
                                          Map<String, Integer> answerDistribution) {
        int totalAnswered = participants.stream().mapToInt(Participant::getAnswerCount).sum();
        int totalCorrect  = participants.stream().mapToInt(Participant::getCorrectCount).sum();
        double totalTime  = participants.stream().mapToDouble(Participant::getTotalResponseTimeSec).sum();

        return new SessionStatsResponse(
                participants.size(),
                session.getCurrentQuestionIndex(),
                totalAnswered == 0 ? 0.0 : Math.round((double) totalCorrect / totalAnswered * 100.0) / 100.0,
                answerDistribution,
                totalAnswered,
                totalAnswered == 0 ? 0.0 : Math.round(totalTime / totalAnswered * 10.0) / 10.0
        );
    }
}
