package com.knupbackend.session.dto.event;

import com.knupbackend.session.domain.SessionStatus;

/** Broadcast to /topic/session/{sessionId}/status — drives client navigation. */
public record SessionStatusEvent(
        String sessionId,
        SessionStatus status,
        String message
) {}
