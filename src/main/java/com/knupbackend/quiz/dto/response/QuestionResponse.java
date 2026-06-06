package com.knupbackend.quiz.dto.response;

import com.knupbackend.quiz.domain.Question;
import com.knupbackend.quiz.domain.QuestionType;

import java.util.List;

public record QuestionResponse(
        Long id,
        String content,
        QuestionType type,
        List<String> options,
        String answer,
        String explanation,
        Integer timeLimit,
        Integer points,
        Integer orderIndex
) {
    public static QuestionResponse from(Question q) {
        return new QuestionResponse(
                q.getId(),
                q.getContent(),
                q.getQuestionType(),
                q.getOptions(),
                q.getCorrectAnswer(),
                q.getExplanation(),
                q.getTimeLimit(),
                q.getPoints(),
                q.getOrderIndex()
        );
    }
}
