package com.knupbackend.ai.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AiQuizGenerateRequest(

        @NotBlank(message = "텍스트는 필수입니다.")
        String text,

        Integer questionCount,
        String questionType,
        String difficulty,
        String language
) {}
