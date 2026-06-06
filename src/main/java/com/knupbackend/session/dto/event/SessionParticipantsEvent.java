package com.knupbackend.session.dto.event;

import java.util.List;

/** Broadcast to /topic/session/{sessionId}/participants on join/leave. */
public record SessionParticipantsEvent(
        String sessionId,
        List<ParticipantInfo> participants,
        int totalCount
) {
    public record ParticipantInfo(
            String participantId,
            String nickname,
            String teamId
    ) {}
}
