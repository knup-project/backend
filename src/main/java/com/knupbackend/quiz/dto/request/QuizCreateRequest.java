package com.knupbackend.quiz.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuizCreateRequest(

        @NotBlank(message = "퀴즈 제목은 필수입니다.")
        @Size(max = 255, message = "제목은 255자 이하여야 합니다.")
        String title,

        String description,

        @Valid
        List<QuestionRequest> questions
) {}
