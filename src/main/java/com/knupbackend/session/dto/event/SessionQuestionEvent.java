package com.knupbackend.session.dto.event;

import com.knupbackend.quiz.domain.QuestionType;

import java.util.List;

/** Broadcast to /topic/session/{sessionId}/question. Never includes the correct answer. */
public record SessionQuestionEvent(
        String sessionId,
        int questionIndex,
        int totalQuestions,
        QuestionPayload question,
        String startedAt
) {
    public record QuestionPayload(
            Long id,
            String content,
            QuestionType type,
            List<String> options,
            int timeLimit,
            int points
    ) {}
}
