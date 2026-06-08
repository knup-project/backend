package com.knupbackend.session.domain;

import com.knupbackend.quiz.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    boolean existsByParticipantAndQuestion(Participant participant, Question question);
    List<Answer> findByParticipant_SessionAndQuestion(GameSession session, Question question);
    void deleteByParticipant(Participant participant);
}
