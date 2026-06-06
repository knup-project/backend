package com.knupbackend.session.dto.response;

import com.knupbackend.session.domain.GameSession;
import com.knupbackend.session.domain.SessionMode;
import com.knupbackend.session.domain.SessionStatus;

import java.time.LocalDateTime;

public record SessionResponse(
        String id,
        String pin,
        Long quizId,
        String quizTitle,
        SessionMode mode,
        SessionStatus status,
        int currentQuestionIndex,
        int totalQuestions,
        Integer maxParticipants,
        int participantCount,
        LocalDateTime createdAt
) {
    public static SessionResponse of(GameSession s, int totalQuestions, int participantCount) {
        return new SessionResponse(
                s.getSessionId(),
                s.getPin(),
                s.getQuiz().getId(),
                s.getQuiz().getTitle(),
                s.getMode(),
                s.getStatus(),
                s.getCurrentQuestionIndex(),
                totalQuestions,
                s.getMaxParticipants(),
                participantCount,
                s.getCreatedAt()
        );
    }
}
