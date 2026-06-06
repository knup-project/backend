package com.knupbackend.session.dto.request;

import jakarta.validation.constraints.NotBlank;

public record JoinSessionRequest(

        @NotBlank(message = "PIN은 필수입니다.")
        String pin,

        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname,

        String teamId
) {}
