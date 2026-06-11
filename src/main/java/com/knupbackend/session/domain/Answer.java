package com.knupbackend.session.domain;

import com.knupbackend.quiz.domain.Question;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "answers",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_answer_participant_question",
                columnNames = {"participant_id", "question_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false)
    private String selectedAnswer;

    private double responseTimeSec;

    private boolean correct;
}
