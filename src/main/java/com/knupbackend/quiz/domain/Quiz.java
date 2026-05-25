package com.knupbackend.quiz.domain;

import com.knupbackend.global.common.BaseEntity;
import com.knupbackend.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Quiz extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    // ── 도메인 메서드 ──────────────────────────────────────────────

    public void update(String title, String description) {
        this.title       = title;
        this.description = description;
    }

    public void updateQuestions(List<Question> newQuestions) {
        this.questions.clear();
        this.questions.addAll(newQuestions);
    }

    public boolean isOwner(Long userId) {
        return this.user.getId().equals(userId);
    }
}
