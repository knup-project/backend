package com.knupbackend.session.dto.response;

public record NextQuestionResponse(
        int questionIndex,
        int totalQuestions,
        boolean isLast
) {}
