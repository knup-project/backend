package com.knupbackend.session.dto.response;

import com.knupbackend.quiz.domain.QuestionType;
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
        CurrentQuestion currentQuestion,
        Integer questionRemainingSec,
        LocalDateTime createdAt
) {
    /** 대기실 인원 스냅샷 (teamId 는 곧 teamName) */
    public record ParticipantSummary(String participantId, String nickname, String teamId) {}

    /**
     * 진행 중(송출된) 문제 스냅샷 — 정답은 절대 포함하지 않는다.
     * WS question 이벤트를 놓친 클라이언트(새로고침/구독 공백)가 REST 로 복구하는 용도.
     */
    public record CurrentQuestion(Long id, String content, QuestionType type,
                                  List<String> options, int timeLimit, int points) {}

    public static SessionResponse of(GameSession s, int totalQuestions, int participantCount,
                                     List<ParticipantSummary> participants) {
        return of(s, totalQuestions, participantCount, participants, null, null);
    }

    public static SessionResponse of(GameSession s, int totalQuestions, int participantCount,
                                     List<ParticipantSummary> participants,
                                     CurrentQuestion currentQuestion, Integer questionRemainingSec) {
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
                currentQuestion,
                questionRemainingSec,
                s.getCreatedAt()
        );
    }
}
