package com.knupbackend.quiz.dto.response;

import com.knupbackend.quiz.domain.Quiz;

import java.time.LocalDateTime;

public record QuizSummaryResponse(
        Long id,
        String title,
        String description,
        int questionCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static QuizSummaryResponse from(Quiz quiz) {
        return new QuizSummaryResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getQuestions().size(),
                quiz.getCreatedAt(),
                quiz.getUpdatedAt()
        );
    }
}
