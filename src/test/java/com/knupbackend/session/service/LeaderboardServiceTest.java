package com.knupbackend.session.service;

import com.knupbackend.global.exception.ErrorCode;
import com.knupbackend.global.exception.KnupException;
import com.knupbackend.quiz.domain.QuestionType;
import com.knupbackend.quiz.domain.Quiz;
import com.knupbackend.quiz.domain.QuizRepository;
import com.knupbackend.session.domain.*;
import com.knupbackend.session.dto.response.LeaderboardResponse;
import com.knupbackend.session.dto.response.SessionStatsResponse;
import com.knupbackend.session.dto.response.TeamLeaderboardResponse;
import com.knupbackend.user.domain.User;
import com.knupbackend.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class LeaderboardServiceTest {

    @Autowired LeaderboardService leaderboardService;
    @Autowired SessionRepository sessionRepository;
    @Autowired ParticipantRepository participantRepository;
    @Autowired AnswerRepository answerRepository;
    @Autowired QuizRepository quizRepository;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private User host;
    private User other;
    private Quiz quiz;
    private GameSession individualSession;
    private GameSession teamSession;

    @BeforeEach
    void setUp() {
        host = userRepository.save(User.builder()
                .email("host@test.com")
                .password(passwordEncoder.encode("pw"))
                .nickname("호스트")
                .build());

        other = userRepository.save(User.builder()
                .email("other@test.com")
                .password(passwordEncoder.encode("pw"))
                .nickname("타인")
                .build());

        quiz = quizRepository.save(Quiz.builder()
                .title("테스트 퀴즈")
                .description("설명")
                .user(host)
                .build());

        individualSession = sessionRepository.save(GameSession.builder()
                .sessionId("session-001")
                .quiz(quiz)
                .mode(SessionMode.INDIVIDUAL)
                .currentQuestionIndex(5)
                .build());

        teamSession = sessionRepository.save(GameSession.builder()
                .sessionId("session-team-001")
                .quiz(quiz)
                .mode(SessionMode.TEAM)
                .currentQuestionIndex(3)
                .build());

        // 개인 세션 참가자 3명 (점수 순서 뒤섞기)
        participantRepository.save(Participant.builder()
                .participantId("p1")
                .session(individualSession)
                .nickname("상민")
                .score(4750)
                .correctCount(5)
                .totalResponseTimeSec(20.5)
                .answerCount(5)
                .build());

        participantRepository.save(Participant.builder()
                .participantId("p2")
                .session(individualSession)
                .nickname("지수")
                .score(3200)
                .correctCount(3)
                .totalResponseTimeSec(30.0)
                .answerCount(3)
                .build());

        participantRepository.save(Participant.builder()
                .participantId("p3")
                .session(individualSession)
                .nickname("민준")
                .score(5000)
                .correctCount(5)
                .totalResponseTimeSec(15.0)
                .answerCount(5)
                .build());

        // 팀 세션 참가자 4명 (A팀 2명, B팀 2명)
        participantRepository.save(Participant.builder()
                .participantId("t1")
                .session(teamSession)
                .nickname("팀A-1")
                .score(12000)
                .correctCount(3)
                .totalResponseTimeSec(18.0)
                .answerCount(3)
                .teamName("A팀")
                .build());

        participantRepository.save(Participant.builder()
                .participantId("t2")
                .session(teamSession)
                .nickname("팀A-2")
                .score(11500)
                .correctCount(3)
                .totalResponseTimeSec(21.0)
                .answerCount(3)
                .teamName("A팀")
                .build());

        participantRepository.save(Participant.builder()
                .participantId("t3")
                .session(teamSession)
                .nickname("팀B-1")
                .score(8000)
                .correctCount(2)
                .totalResponseTimeSec(24.0)
                .answerCount(2)
                .teamName("B팀")
                .build());

        participantRepository.save(Participant.builder()
                .participantId("t4")
                .session(teamSession)
                .nickname("팀B-2")
                .score(7500)
                .correctCount(2)
                .totalResponseTimeSec(30.0)
                .answerCount(2)
                .teamName("B팀")
                .build());
    }

    // ── 5.1 개인 리더보드 ───────────────────────────────────────────

    @Test
    @DisplayName("개인 리더보드 - 점수 내림차순 top N 반환")
    void getLeaderboard_sortedByScoreDesc() {
        LeaderboardResponse result = leaderboardService.getLeaderboard("session-001", 2);

        assertThat(result.sessionId()).isEqualTo("session-001");
        assertThat(result.currentQuestion()).isEqualTo(5);
        assertThat(result.entries()).hasSize(2);
        assertThat(result.entries().get(0).rank()).isEqualTo(1);
        assertThat(result.entries().get(0).nickname()).isEqualTo("민준");   // 5000점
        assertThat(result.entries().get(1).nickname()).isEqualTo("상민");   // 4750점
    }

    @Test
    @DisplayName("개인 리더보드 - averageTimeSec 계산 정확도 검증")
    void getLeaderboard_averageTimeCalculated() {
        LeaderboardResponse result = leaderboardService.getLeaderboard("session-001", 10);

        // 민준: 15.0 / 5 = 3.0
        double avgTime = result.entries().get(0).averageTimeSec();
        assertThat(avgTime).isEqualTo(3.0);
    }

    @Test
    @DisplayName("개인 리더보드 - 없는 세션 → SESSION_NOT_FOUND 예외")
    void getLeaderboard_sessionNotFound() {
        assertThatThrownBy(() -> leaderboardService.getLeaderboard("no-such-session", 10))
                .isInstanceOf(KnupException.class)
                .satisfies(e ->
                        assertThat(((KnupException) e).getErrorCode())
                                .isEqualTo(ErrorCode.SESSION_NOT_FOUND));
    }

    // ── 5.2 팀 리더보드 ─────────────────────────────────────────────

    @Test
    @DisplayName("팀 리더보드 - 팀 총점 내림차순 정렬, 멤버 포함")
    void getTeamLeaderboard_sortedByTotalScore() {
        TeamLeaderboardResponse result = leaderboardService.getTeamLeaderboard("session-team-001");

        assertThat(result.entries()).hasSize(2);
        assertThat(result.entries().get(0).rank()).isEqualTo(1);
        assertThat(result.entries().get(0).teamName()).isEqualTo("A팀");   // 23500점
        assertThat(result.entries().get(0).totalScore()).isEqualTo(23500);
        assertThat(result.entries().get(0).memberCount()).isEqualTo(2);
        assertThat(result.entries().get(0).members()).hasSize(2);
        assertThat(result.entries().get(1).teamName()).isEqualTo("B팀");   // 15500점
    }

    @Test
    @DisplayName("팀 리더보드 - INDIVIDUAL 세션 → NOT_TEAM_MODE 예외")
    void getTeamLeaderboard_notTeamMode_throws() {
        assertThatThrownBy(() -> leaderboardService.getTeamLeaderboard("session-001"))
                .isInstanceOf(KnupException.class)
                .satisfies(e ->
                        assertThat(((KnupException) e).getErrorCode())
                                .isEqualTo(ErrorCode.NOT_TEAM_MODE));
    }

    @Test
    @DisplayName("팀 리더보드 - 없는 세션 → SESSION_NOT_FOUND 예외")
    void getTeamLeaderboard_sessionNotFound() {
        assertThatThrownBy(() -> leaderboardService.getTeamLeaderboard("no-such"))
                .isInstanceOf(KnupException.class)
                .satisfies(e ->
                        assertThat(((KnupException) e).getErrorCode())
                                .isEqualTo(ErrorCode.SESSION_NOT_FOUND));
    }

    // ── 5.3 실시간 통계 ─────────────────────────────────────────────

    @Test
    @DisplayName("통계 - 참가자 수, 정답률, 평균 응답 시간 정확도 검증")
    void getStats_correctAggregation() {
        // p1(맞힘 5/5, 응답 20.5s), p2(3/3, 30s), p3(5/5, 15s)
        // totalParticipants=3, correctCount 합=13, answerCount 합=13 → accuracy=1.0
        // totalTime=20.5+30+15=65.5, count=13 → avgTime=65.5/13
        SessionStatsResponse result = leaderboardService.getStats("session-001", host);

        assertThat(result.totalParticipants()).isEqualTo(3);
        assertThat(result.currentQuestionIndex()).isEqualTo(5);
        assertThat(result.overallAccuracy()).isEqualTo(1.0);
        assertThat(result.answeredCount()).isEqualTo(13);
    }

    @Test
    @DisplayName("통계 - 호스트가 아닌 사용자 → SESSION_ACCESS_DENIED 예외")
    void getStats_nonHost_throws() {
        assertThatThrownBy(() -> leaderboardService.getStats("session-001", other))
                .isInstanceOf(KnupException.class)
                .satisfies(e ->
                        assertThat(((KnupException) e).getErrorCode())
                                .isEqualTo(ErrorCode.SESSION_ACCESS_DENIED));
    }

    @Test
    @DisplayName("통계 - 없는 세션 → SESSION_NOT_FOUND 예외")
    void getStats_sessionNotFound() {
        assertThatThrownBy(() -> leaderboardService.getStats("no-such", host))
                .isInstanceOf(KnupException.class)
                .satisfies(e ->
                        assertThat(((KnupException) e).getErrorCode())
                                .isEqualTo(ErrorCode.SESSION_NOT_FOUND));
    }
}
