package com.knupbackend.session.service;

import com.knupbackend.global.exception.ErrorCode;
import com.knupbackend.global.exception.KnupException;
import com.knupbackend.session.domain.*;
import com.knupbackend.session.dto.response.LeaderboardResponse;
import com.knupbackend.session.dto.response.SessionStatsResponse;
import com.knupbackend.session.dto.response.TeamLeaderboardResponse;
import com.knupbackend.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaderboardService {

    private final SessionRepository sessionRepository;
    private final ParticipantRepository participantRepository;
    private final AnswerRepository answerRepository;

    /** 5.1 개인 리더보드 */
    public LeaderboardResponse getLeaderboard(String sessionId, int top) {
        GameSession session = findSession(sessionId);
        List<Participant> sorted =
                participantRepository.findBySessionOrderByScoreDesc(session);
        return LeaderboardResponse.of(session, sorted, top);
    }

    /** 5.2 팀 리더보드 */
    public TeamLeaderboardResponse getTeamLeaderboard(String sessionId) {
        GameSession session = findSession(sessionId);
        if (!session.isTeamMode()) {
            throw new KnupException(ErrorCode.NOT_TEAM_MODE);
        }
        List<Participant> participants = participantRepository.findBySession(session);
        return TeamLeaderboardResponse.of(participants);
    }

    /** 5.3 실시간 통계 (호스트 전용) */
    public SessionStatsResponse getStats(String sessionId, User user) {
        GameSession session = findSession(sessionId);
        if (!session.isHostedBy(user.getId())) {
            throw new KnupException(ErrorCode.SESSION_ACCESS_DENIED);
        }

        List<Participant> participants = participantRepository.findBySession(session);

        // answerDistribution: 세션 내 선택지별 응답 수
        Map<String, Integer> distribution = answerRepository.findByParticipant_Session(session).stream()
                .collect(Collectors.toMap(
                        Answer::getSelectedAnswer,
                        a -> 1,
                        Integer::sum
                ));

        return SessionStatsResponse.of(session, participants, distribution);
    }

    // ── private ──────────────────────────────────────────────────

    private GameSession findSession(String sessionId) {
        return sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new KnupException(ErrorCode.SESSION_NOT_FOUND));
    }
}
