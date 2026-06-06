package com.knupbackend.session.service;

import com.knupbackend.session.dto.event.SessionParticipantsEvent;
import com.knupbackend.session.dto.event.SessionQuestionEvent;
import com.knupbackend.session.dto.event.SessionResultEvent;
import com.knupbackend.session.dto.event.SessionStatusEvent;
import com.knupbackend.session.dto.response.LeaderboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Sends real-time events to the per-session STOMP topics the frontend subscribes to. */
@Component
@RequiredArgsConstructor
public class SessionBroadcaster {

    private static final String PREFIX = "/topic/session/";

    private final SimpMessagingTemplate messaging;

    public void question(SessionQuestionEvent event) {
        messaging.convertAndSend(PREFIX + event.sessionId() + "/question", event);
    }

    public void result(SessionResultEvent event) {
        messaging.convertAndSend(PREFIX + event.sessionId() + "/result", event);
    }

    public void leaderboard(LeaderboardResponse event) {
        messaging.convertAndSend(PREFIX + event.sessionId() + "/leaderboard", event);
    }

    public void status(SessionStatusEvent event) {
        messaging.convertAndSend(PREFIX + event.sessionId() + "/status", event);
    }

    public void participants(SessionParticipantsEvent event) {
        messaging.convertAndSend(PREFIX + event.sessionId() + "/participants", event);
    }
}
