package com.knupbackend.quiz.dto.response;

import com.knupbackend.quiz.domain.Question;
import com.knupbackend.quiz.domain.QuestionType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class QuestionResponse {

    private Long id;
    private String content;
    private QuestionType questionType;
    private List<String> options;
    private String correctAnswer;
    private Integer timeLimit;
    private Integer points;
    private Integer orderIndex;

    public static QuestionResponse from(Question q) {
        return QuestionResponse.builder()
                .id(q.getId())
                .content(q.getContent())
                .questionType(q.getQuestionType())
                .options(q.getOptions())
                .correctAnswer(q.getCorrectAnswer())
                .timeLimit(q.getTimeLimit())
                .points(q.getPoints())
                .orderIndex(q.getOrderIndex())
                .build();
    }
}
