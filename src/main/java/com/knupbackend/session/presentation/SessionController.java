package com.knupbackend.session.presentation;

import com.knupbackend.global.auth.LoginUser;
import com.knupbackend.session.dto.request.AnswerSubmitRequest;
import com.knupbackend.session.dto.request.JoinSessionRequest;
import com.knupbackend.session.dto.request.KickParticipantsRequest;
import com.knupbackend.session.dto.request.SessionCreateRequest;
import com.knupbackend.session.dto.response.*;
import com.knupbackend.session.service.SessionService;
import com.knupbackend.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    /** 세션 생성 (호스트) */
    @PostMapping
    public ResponseEntity<SessionResponse> create(@Valid @RequestBody SessionCreateRequest request,
                                                  @LoginUser User host) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionService.create(request, host));
    }

    /** 세션 조회 (호스트) */
    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionResponse> get(@PathVariable String sessionId) {
        return ResponseEntity.ok(sessionService.get(sessionId));
    }

    /** PIN으로 입장 (참가자, 인증 불필요) */
    @PostMapping("/join")
    public ResponseEntity<JoinSessionResponse> join(@Valid @RequestBody JoinSessionRequest request) {
        return ResponseEntity.ok(sessionService.join(request));
    }

    /** 세션 시작 (호스트) */
    @PostMapping("/{sessionId}/start")
    public ResponseEntity<Void> start(@PathVariable String sessionId, @LoginUser User host) {
        sessionService.start(sessionId, host);
        return ResponseEntity.ok().build();
    }

    /** 다음 문제 송출 (호스트) */
    @PostMapping("/{sessionId}/next")
    public ResponseEntity<NextQuestionResponse> next(@PathVariable String sessionId, @LoginUser User host) {
        return ResponseEntity.ok(sessionService.next(sessionId, host));
    }

    /** 세션 종료 (호스트) */
    @PostMapping("/{sessionId}/end")
    public ResponseEntity<EndSessionResponse> end(@PathVariable String sessionId, @LoginUser User host) {
        return ResponseEntity.ok(sessionService.end(sessionId, host));
    }

    /** 답안 제출 (참가자, X-Participant-Id 헤더로 식별) */
    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<AnswerResultResponse> answer(@PathVariable String sessionId,
                                                       @RequestHeader("X-Participant-Id") String participantId,
                                                       @Valid @RequestBody AnswerSubmitRequest request) {
        return ResponseEntity.ok(sessionService.submitAnswer(sessionId, participantId, request));
    }

    /** 참가자 강퇴 (호스트) */
    @DeleteMapping("/{sessionId}/participants/{participantId}")
    public ResponseEntity<Void> kickParticipant(@PathVariable String sessionId,
                                                @PathVariable String participantId,
                                                @LoginUser User host) {
        sessionService.removeParticipant(sessionId, participantId, host);
        return ResponseEntity.ok().build();
    }

    /** 참가자 일괄/전체 강퇴 (호스트) — participantIds 가 비어 있으면 전체 */
    @PostMapping("/{sessionId}/participants/kick")
    public ResponseEntity<Void> kickParticipants(@PathVariable String sessionId,
                                                 @RequestBody KickParticipantsRequest request,
                                                 @LoginUser User host) {
        sessionService.removeParticipants(sessionId, request.participantIds(), host);
        return ResponseEntity.ok().build();
    }
}
