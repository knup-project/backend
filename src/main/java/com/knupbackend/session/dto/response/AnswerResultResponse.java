package com.knupbackend.session.dto.response;

public record AnswerResultResponse(
        boolean correct,
        String correctAnswer,
        int points,
        int totalPoints,
        int rank
) {}
