package com.knupbackend.quiz.dto.response;

import com.knupbackend.quiz.entity.Quiz;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class QuizSummaryResponse {

    private Long id;
    private String title;
    private String description;
    private int questionCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static QuizSummaryResponse from(Quiz quiz) {
        return QuizSummaryResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .questionCount(quiz.getQuestions().size())
                .createdAt(quiz.getCreatedAt())
                .updatedAt(quiz.getUpdatedAt())
                .build();
    }
}
