package com.knupbackend.ai.dto.response;

import com.knupbackend.quiz.dto.request.QuestionRequest;

import java.util.List;

public record AiQuizGenerateResponse(
        List<QuestionRequest> questions,
        String generatedAt
) {}
