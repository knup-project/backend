package com.knupbackend.session.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<GameSession, Long> {
    Optional<GameSession> findBySessionId(String sessionId);
    Optional<GameSession> findByPin(String pin);
    boolean existsBySessionId(String sessionId);
    boolean existsByPin(String pin);

    /** 종료되지 않은 세션 (유령 세션 정리용 — 활동 시각 필터는 서비스에서) */
    List<GameSession> findByStatusNot(SessionStatus status);
}
