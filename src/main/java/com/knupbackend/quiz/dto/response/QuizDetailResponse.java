package com.knupbackend.quiz.dto.response;

import com.knupbackend.quiz.domain.Quiz;

import java.time.LocalDateTime;
import java.util.List;

public record QuizDetailResponse(
        Long id,
        String title,
        String description,
        List<QuestionResponse> questions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static QuizDetailResponse from(Quiz quiz) {
        return new QuizDetailResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getQuestions().stream().map(QuestionResponse::from).toList(),
                quiz.getCreatedAt(),
                quiz.getUpdatedAt()
        );
    }
}
