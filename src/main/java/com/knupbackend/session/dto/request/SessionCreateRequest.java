package com.knupbackend.session.dto.request;

import com.knupbackend.session.domain.SessionMode;
import jakarta.validation.constraints.NotNull;

public record SessionCreateRequest(

        @NotNull(message = "퀴즈 ID는 필수입니다.")
        Long quizId,

        @NotNull(message = "세션 모드는 필수입니다.")
        SessionMode mode,

        Integer teamCount,

        Integer maxParticipants
) {}
