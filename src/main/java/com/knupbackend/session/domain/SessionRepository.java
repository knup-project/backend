package com.knupbackend.session.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<GameSession, Long> {
    Optional<GameSession> findBySessionId(String sessionId);
    Optional<GameSession> findByPin(String pin);
    boolean existsBySessionId(String sessionId);
    boolean existsByPin(String pin);

    /** 종료되지 않았고 생성 시각이 cutoff 이전인 세션 (유령 세션 정리용) */
    List<GameSession> findByStatusNotAndCreatedAtBefore(SessionStatus status, LocalDateTime cutoff);
}
