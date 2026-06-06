package com.knupbackend.session.domain;

import com.knupbackend.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "participants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Participant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String participantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private GameSession session;

    @Column(nullable = false)
    private String nickname;

    @Builder.Default
    private int score = 0;

    @Builder.Default
    private int correctCount = 0;

    /** 누적 응답 시간 (초) */
    @Builder.Default
    private double totalResponseTimeSec = 0.0;

    /** 응답한 문제 수 (averageTimeSec 계산 기준) */
    @Builder.Default
    private int answerCount = 0;

    /** TEAM 모드에서만 사용 */
    private String teamName;

    public double averageResponseTimeSec() {
        if (answerCount == 0) return 0.0;
        return totalResponseTimeSec / answerCount;
    }

    /** Applies one graded answer to this participant's running totals. */
    public void recordAnswer(boolean correct, double responseTimeSec, int awardedPoints) {
        this.score += awardedPoints;
        if (correct) {
            this.correctCount++;
        }
        this.totalResponseTimeSec += responseTimeSec;
        this.answerCount++;
    }
}
