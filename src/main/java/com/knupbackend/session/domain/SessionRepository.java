package com.knupbackend.session.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<GameSession, Long> {
    Optional<GameSession> findBySessionId(String sessionId);
    Optional<GameSession> findByPin(String pin);
    boolean existsBySessionId(String sessionId);
    boolean existsByPin(String pin);
}
