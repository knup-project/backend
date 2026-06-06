package com.knupbackend.session.presentation;

import com.knupbackend.session.dto.request.AnswerSubmitRequest;
import com.knupbackend.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

/**
 * STOMP message handlers. The shipped frontend submits answers over REST and only
 * publishes heartbeats here, but the answer channel is implemented per the contract.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class SessionMessageController {

    private final SessionService sessionService;

    @MessageMapping("/session/{sessionId}/answer")
    public void answer(@DestinationVariable String sessionId, AnswerMessage message) {
        try {
            sessionService.submitAnswer(
                    sessionId,
                    message.participantId(),
                    new AnswerSubmitRequest(message.questionId(), message.answer(), null)
            );
        } catch (Exception e) {
            log.warn("WS answer failed for session {}: {}", sessionId, e.getMessage());
        }
    }

    @MessageMapping("/session/{sessionId}/heartbeat")
    public void heartbeat(@DestinationVariable String sessionId, HeartbeatMessage message) {
        // Presence ping — accepted to keep the connection live; no state change needed.
    }

    public record AnswerMessage(String participantId, Long questionId, String answer) {}

    public record HeartbeatMessage(String participantId) {}
}
