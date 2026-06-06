package com.knupbackend.session.dto.event;

import java.util.Map;

/** Broadcast to /topic/session/{sessionId}/result (aggregate stats for the current question). */
public record SessionResultEvent(
        String sessionId,
        Long questionId,
        String correctAnswer,
        Map<String, Integer> answerDistribution,
        int answeredCount,
        double accuracy
) {}
