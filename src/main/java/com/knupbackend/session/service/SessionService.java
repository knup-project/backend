package com.knupbackend.session.service;

import com.knupbackend.global.exception.ErrorCode;
import com.knupbackend.global.exception.KnupException;
import com.knupbackend.quiz.domain.Question;
import com.knupbackend.quiz.domain.Quiz;
import com.knupbackend.quiz.domain.QuizRepository;
import com.knupbackend.session.domain.*;
import com.knupbackend.session.dto.event.SessionParticipantsEvent;
import com.knupbackend.session.dto.event.SessionQuestionEvent;
import com.knupbackend.session.dto.event.SessionResultEvent;
import com.knupbackend.session.dto.event.SessionStatusEvent;
import com.knupbackend.session.dto.request.AnswerSubmitRequest;
import com.knupbackend.session.dto.request.JoinSessionRequest;
import com.knupbackend.session.dto.request.SessionCreateRequest;
import com.knupbackend.session.dto.response.*;
import com.knupbackend.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionService {

    private static final int LEADERBOARD_TOP = 5;
    private static final int DEFAULT_TIME_LIMIT = 20;
    private static final int DEFAULT_POINTS = 100;

    private final SessionRepository sessionRepository;
    private final ParticipantRepository participantRepository;
    private final AnswerRepository answerRepository;
    private final QuizRepository quizRepository;
    private final SessionBroadcaster broadcaster;

    // ── Host: lifecycle ───────────────────────────────────────────

    @Transactional
    public SessionResponse create(SessionCreateRequest request, User host) {
        Quiz quiz = quizRepository.findById(request.quizId())
                .orElseThrow(() -> new KnupException(ErrorCode.QUIZ_NOT_FOUND));
        if (!quiz.isOwner(host.getId())) {
            throw new KnupException(ErrorCode.QUIZ_ACCESS_DENIED);
        }

        GameSession session = GameSession.builder()
                .sessionId(generateSessionId())
                .pin(generatePin())
                .quiz(quiz)
                .mode(request.mode())
                .maxParticipants(request.maxParticipants())
                .build();

        sessionRepository.save(session);
        return SessionResponse.of(session, quiz.getQuestions().size(), 0, List.of());
    }

    public SessionResponse get(String sessionId) {
        GameSession session = findSession(sessionId);
        List<Participant> participants = participantRepository.findBySession(session);
        List<SessionResponse.ParticipantSummary> summaries = participants.stream()
                .map(p -> new SessionResponse.ParticipantSummary(
                        p.getParticipantId(), p.getNickname(), p.getTeamName()))
                .toList();
        return SessionResponse.of(
                session,
                session.getQuiz().getQuestions().size(),
                participants.size(),
                summaries
        );
    }

    @Transactional
    public void start(String sessionId, User host) {
        GameSession session = findSession(sessionId);
        requireHost(session, host);
        if (!session.isWaiting()) {
            throw new KnupException(ErrorCode.SESSION_ALREADY_STARTED);
        }
        session.start();
        broadcaster.status(new SessionStatusEvent(session.getSessionId(), SessionStatus.IN_PROGRESS, null));
    }

    @Transactional
    public NextQuestionResponse next(String sessionId, User host) {
        GameSession session = findSession(sessionId);
        requireHost(session, host);
        if (!session.isInProgress()) {
            throw new KnupException(ErrorCode.SESSION_NOT_STARTED);
        }

        if (session.currentQuestionServed()) {
            session.advanceToNextQuestion();
        }

        List<Question> questions = session.getQuiz().getQuestions();
        int total = questions.size();
        int idx = session.getCurrentQuestionIndex();
        if (idx >= total) {
            throw new KnupException(ErrorCode.NO_MORE_QUESTIONS);
        }

        Question question = questions.get(idx);
        session.serveCurrentQuestion();

        broadcaster.question(new SessionQuestionEvent(
                session.getSessionId(),
                idx,
                total,
                toPayload(question),
                java.time.LocalDateTime.now().toString()
        ));

        return new NextQuestionResponse(idx, total, idx == total - 1);
    }

    @Transactional
    public EndSessionResponse end(String sessionId, User host) {
        GameSession session = findSession(sessionId);
        requireHost(session, host);
        session.end();
        broadcaster.status(new SessionStatusEvent(session.getSessionId(), SessionStatus.FINISHED, null));

        List<Participant> sorted = participantRepository.findBySessionOrderByScoreDesc(session);
        List<LeaderboardEntry> finalBoard = rankAll(sorted);
        return new EndSessionResponse(finalBoard, sorted.size(), session.durationSeconds());
    }

    // ── Participant ───────────────────────────────────────────────

    @Transactional
    public JoinSessionResponse join(JoinSessionRequest request) {
        GameSession session = sessionRepository.findByPin(request.pin())
                .orElseThrow(() -> new KnupException(ErrorCode.INVALID_PIN));
        if (!session.isWaiting()) {
            throw new KnupException(ErrorCode.SESSION_ALREADY_STARTED);
        }
        Integer max = session.getMaxParticipants();
        if (max != null && participantRepository.countBySession(session) >= max) {
            throw new KnupException(ErrorCode.SESSION_FULL);
        }
        if (participantRepository.existsBySessionAndNickname(session, request.nickname())) {
            throw new KnupException(ErrorCode.NICKNAME_DUPLICATE);
        }

        Participant participant = Participant.builder()
                .participantId(UUID.randomUUID().toString())
                .session(session)
                .nickname(request.nickname())
                .teamName(request.teamId())
                .build();
        participantRepository.save(participant);

        broadcastParticipants(session);

        return new JoinSessionResponse(
                participant.getParticipantId(),
                session.getSessionId(),
                participant.getNickname(),
                request.teamId()
        );
    }

    @Transactional
    public AnswerResultResponse submitAnswer(String sessionId, String participantId, AnswerSubmitRequest request) {
        GameSession session = findSession(sessionId);

        Participant participant = participantRepository.findByParticipantId(participantId)
                .orElseThrow(() -> new KnupException(ErrorCode.PARTICIPANT_NOT_FOUND));
        if (!participant.getSession().getSessionId().equals(sessionId)) {
            throw new KnupException(ErrorCode.PARTICIPANT_NOT_FOUND);
        }

        Question question = session.getQuiz().getQuestions().stream()
                .filter(q -> q.getId().equals(request.questionId()))
                .findFirst()
                .orElseThrow(() -> new KnupException(ErrorCode.QUESTION_NOT_FOUND));

        if (answerRepository.existsByParticipantAndQuestion(participant, question)) {
            throw new KnupException(ErrorCode.ALREADY_SUBMITTED);
        }

        double responseTime = resolveResponseTime(session, request.responseTimeSec());
        boolean correct = isCorrect(question, request.answer());
        int awarded = scorePoints(correct, question, responseTime);

        participant.recordAnswer(correct, responseTime, awarded);
        answerRepository.save(Answer.builder()
                .participant(participant)
                .question(question)
                .selectedAnswer(request.answer())
                .responseTimeSec(responseTime)
                .correct(correct)
                .build());

        List<Participant> sorted = participantRepository.findBySessionOrderByScoreDesc(session);
        int rank = rankOf(sorted, participant);

        broadcastResult(session, question);
        broadcaster.leaderboard(LeaderboardResponse.of(session, sorted, LEADERBOARD_TOP));

        return new AnswerResultResponse(
                correct,
                question.getCorrectAnswer(),
                awarded,
                participant.getScore(),
                rank
        );
    }

    // ── Host: 참가자 강퇴 ─────────────────────────────────────────

    @Transactional
    public void removeParticipant(String sessionId, String participantId, User host) {
        GameSession session = findSession(sessionId);
        requireHost(session, host);

        Participant participant = participantRepository.findByParticipantId(participantId)
                .orElseThrow(() -> new KnupException(ErrorCode.PARTICIPANT_NOT_FOUND));
        if (!participant.getSession().getSessionId().equals(sessionId)) {
            throw new KnupException(ErrorCode.PARTICIPANT_NOT_FOUND);
        }

        answerRepository.deleteByParticipant(participant);
        participantRepository.delete(participant);
        broadcastParticipants(session);
    }

    // ── helpers ───────────────────────────────────────────────────

    private GameSession findSession(String sessionId) {
        return sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new KnupException(ErrorCode.SESSION_NOT_FOUND));
    }

    private void requireHost(GameSession session, User host) {
        if (!session.isHostedBy(host.getId())) {
            throw new KnupException(ErrorCode.SESSION_ACCESS_DENIED);
        }
    }

    private SessionQuestionEvent.QuestionPayload toPayload(Question q) {
        return new SessionQuestionEvent.QuestionPayload(
                q.getId(),
                q.getContent(),
                q.getQuestionType(),
                q.getOptions(),
                q.getTimeLimit() != null ? q.getTimeLimit() : DEFAULT_TIME_LIMIT,
                q.getPoints() != null ? q.getPoints() : DEFAULT_POINTS
        );
    }

    private void broadcastResult(GameSession session, Question question) {
        List<Answer> answers = answerRepository.findByParticipant_SessionAndQuestion(session, question);
        Map<String, Integer> distribution = answers.stream()
                .collect(Collectors.toMap(Answer::getSelectedAnswer, a -> 1, Integer::sum));
        int answered = answers.size();
        long correctCount = answers.stream().filter(Answer::isCorrect).count();
        double accuracy = answered == 0 ? 0.0 : Math.round((double) correctCount / answered * 100.0) / 100.0;

        broadcaster.result(new SessionResultEvent(
                session.getSessionId(),
                question.getId(),
                question.getCorrectAnswer(),
                distribution,
                answered,
                accuracy
        ));
    }

    private void broadcastParticipants(GameSession session) {
        List<SessionParticipantsEvent.ParticipantInfo> infos = participantRepository.findBySession(session).stream()
                .map(p -> new SessionParticipantsEvent.ParticipantInfo(
                        p.getParticipantId(), p.getNickname(), p.getTeamName()))
                .toList();
        broadcaster.participants(new SessionParticipantsEvent(session.getSessionId(), infos, infos.size()));
    }

    private double resolveResponseTime(GameSession session, Double provided) {
        if (provided != null && provided >= 0) {
            return Math.round(provided * 10.0) / 10.0;
        }
        if (session.getCurrentQuestionStartedAt() != null) {
            long ms = java.time.Duration.between(session.getCurrentQuestionStartedAt(),
                    java.time.LocalDateTime.now()).toMillis();
            return Math.round(ms / 100.0) / 10.0;
        }
        return 0.0;
    }

    private boolean isCorrect(Question question, String answer) {
        if (question.getCorrectAnswer() == null) return false;
        return question.getCorrectAnswer().trim().equalsIgnoreCase(answer.trim());
    }

    private int scorePoints(boolean correct, Question question, double responseTime) {
        if (!correct) return 0;
        int base = question.getPoints() != null ? question.getPoints() : DEFAULT_POINTS;
        Integer timeLimit = question.getTimeLimit();
        if (timeLimit == null || timeLimit <= 0) return base;
        double remainingRatio = Math.max(0.0, (timeLimit - responseTime) / timeLimit);
        return base + (int) Math.round(base * remainingRatio);
    }

    private List<LeaderboardEntry> rankAll(List<Participant> sorted) {
        return java.util.stream.IntStream.range(0, sorted.size())
                .mapToObj(i -> LeaderboardEntry.of(i + 1, sorted.get(i)))
                .toList();
    }

    private int rankOf(List<Participant> sorted, Participant target) {
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getParticipantId().equals(target.getParticipantId())) {
                return i + 1;
            }
        }
        return sorted.size();
    }

    private String generateSessionId() {
        String id;
        do {
            id = UUID.randomUUID().toString();
        } while (sessionRepository.existsBySessionId(id));
        return id;
    }

    private String generatePin() {
        for (int i = 0; i < 50; i++) {
            String pin = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
            if (!sessionRepository.existsByPin(pin)) {
                return pin;
            }
        }
        throw new KnupException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
