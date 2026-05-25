package com.knupbackend.session.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findBySessionOrderByScoreDesc(GameSession session);
    List<Participant> findBySession(GameSession session);
}
