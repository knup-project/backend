package com.knupbackend.session.domain;

import com.knupbackend.global.common.BaseEntity;
import com.knupbackend.quiz.domain.Quiz;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false, unique = true)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionMode mode;

    @Column(nullable = false)
    @Builder.Default
    private int currentQuestionIndex = 0;

    public boolean isTeamMode() {
        return this.mode == SessionMode.TEAM;
    }

    public boolean isHostedBy(Long userId) {
        return this.quiz.getUser().getId().equals(userId);
    }
}
