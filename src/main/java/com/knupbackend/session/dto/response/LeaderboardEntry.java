package com.knupbackend.session.dto.response;

import com.knupbackend.session.domain.Participant;
import lombok.Getter;

@Getter
public class LeaderboardEntry {
    private final int rank;
    private final String participantId;
    private final String nickname;
    private final int score;
    private final int correctCount;
    private final double averageTimeSec;

    public LeaderboardEntry(int rank, Participant p) {
        this.rank            = rank;
        this.participantId   = p.getParticipantId();
        this.nickname        = p.getNickname();
        this.score           = p.getScore();
        this.correctCount    = p.getCorrectCount();
        this.averageTimeSec  = Math.round(p.averageResponseTimeSec() * 10.0) / 10.0;
    }
}
