package com.knupbackend.quiz.dto.request;

import com.knupbackend.quiz.domain.QuestionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record QuestionRequest(

        @NotBlank(message = "문제 내용은 필수입니다.")
        String content,

        @NotNull(message = "문제 유형은 필수입니다.")
        QuestionType questionType,

        List<String> options,

        String correctAnswer,

        @Min(value = 5, message = "제한 시간은 최소 5초 이상이어야 합니다.")
        Integer timeLimit,

        @Min(value = 1, message = "점수는 최소 1점 이상이어야 합니다.")
        Integer points
) {}
