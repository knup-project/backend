package com.knupbackend.session.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnswerSubmitRequest(

        @NotNull(message = "문제 ID는 필수입니다.")
        Long questionId,

        @NotBlank(message = "답안은 필수입니다.")
        String answer,

        Double responseTimeSec
) {}
