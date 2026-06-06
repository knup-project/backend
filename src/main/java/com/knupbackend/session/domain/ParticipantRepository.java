package com.knupbackend.session.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findBySessionOrderByScoreDesc(GameSession session);
    List<Participant> findBySession(GameSession session);
    Optional<Participant> findByParticipantId(String participantId);
    int countBySession(GameSession session);
    boolean existsBySessionAndNickname(GameSession session, String nickname);
}
