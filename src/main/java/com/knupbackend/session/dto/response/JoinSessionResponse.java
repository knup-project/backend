package com.knupbackend.session.dto.response;

public record JoinSessionResponse(
        String participantId,
        String sessionId,
        String nickname,
        String teamId
) {}
