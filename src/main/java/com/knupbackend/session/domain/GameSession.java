package com.knupbackend.session.domain;

import com.knupbackend.global.common.BaseEntity;
import com.knupbackend.quiz.domain.Quiz;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GameSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Public session identifier (used in URLs and STOMP topics). */
    @Column(nullable = false, unique = true)
    private String sessionId;

    /** Numeric join code participants type in. */
    @Column(nullable = false, unique = true)
    private String pin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SessionStatus status = SessionStatus.WAITING;

    /** Index (0-based) of the question currently being shown. */
    @Column(nullable = false)
    @Builder.Default
    private int currentQuestionIndex = 0;

    private Integer maxParticipants;

    /** TEAM 모드에서 자동 배정에 사용할 팀 수 (null 이면 기본값). */
    private Integer teamCount;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    /** Set when the current question is broadcast; null until it is served. */
    private LocalDateTime currentQuestionStartedAt;

    /** 마지막 호스트/참가자 활동 시각 — 유령 세션 정리 기준 (null 이면 createdAt 사용). */
    private LocalDateTime lastActivityAt;

    public boolean isTeamMode() {
        return this.mode == SessionMode.TEAM;
    }

    public boolean isHostedBy(Long userId) {
        return this.quiz.getUser().getId().equals(userId);
    }

    public boolean isWaiting() {
        return this.status == SessionStatus.WAITING;
    }

    public boolean isInProgress() {
        return this.status == SessionStatus.IN_PROGRESS;
    }

    public boolean isFinished() {
        return this.status == SessionStatus.FINISHED;
    }

    public boolean currentQuestionServed() {
        return this.currentQuestionStartedAt != null;
    }

    public void start() {
        this.status = SessionStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
        this.currentQuestionIndex = 0;
        this.currentQuestionStartedAt = null;
        touch();
    }

    public void serveCurrentQuestion() {
        this.currentQuestionStartedAt = LocalDateTime.now();
        touch();
    }

    /** 활동 시각 갱신 (참가/시작/문제 송출 시 호출). */
    public void touch() {
        this.lastActivityAt = LocalDateTime.now();
    }

    /** 유령 세션 판정 기준 시각: 마지막 활동이 없으면 생성 시각. */
    public LocalDateTime effectiveActivityAt() {
        return lastActivityAt != null ? lastActivityAt : getCreatedAt();
    }

    public void advanceToNextQuestion() {
        this.currentQuestionIndex++;
        this.currentQuestionStartedAt = null;
    }

    public void end() {
        this.status = SessionStatus.FINISHED;
        this.endedAt = LocalDateTime.now();
    }

    public long durationSeconds() {
        if (startedAt == null) return 0L;
        LocalDateTime finish = (endedAt != null) ? endedAt : LocalDateTime.now();
        return Math.max(0L, Duration.between(startedAt, finish).getSeconds());
    }
}
