package com.knupbackend.session.dto.response;

import com.knupbackend.session.domain.GameSession;
import com.knupbackend.session.domain.SessionMode;
import com.knupbackend.session.domain.SessionStatus;

import java.time.LocalDateTime;
import java.util.List;

public record SessionResponse(
        String sessionId,
        String pin,
        Long quizId,
        String quizTitle,
        SessionMode mode,
        SessionStatus status,
        int currentQuestionIndex,
        int totalQuestions,
        Integer maxParticipants,
        int participantCount,
        List<ParticipantSummary> participants,
        LocalDateTime createdAt
) {
    /** 대기실 인원 스냅샷 (teamId 는 곧 teamName) */
    public record ParticipantSummary(String participantId, String nickname, String teamId) {}

    public static SessionResponse of(GameSession s, int totalQuestions, int participantCount,
                                     List<ParticipantSummary> participants) {
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
                participants,
                s.getCreatedAt()
        );
    }
}
