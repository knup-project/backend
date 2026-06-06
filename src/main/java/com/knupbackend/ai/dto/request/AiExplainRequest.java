package com.knupbackend.ai.dto.request;

import jakarta.validation.constraints.NotNull;

public record AiExplainRequest(

        @NotNull(message = "문제 ID는 필수입니다.")
        Long questionId,

        String participantAnswer
) {}
