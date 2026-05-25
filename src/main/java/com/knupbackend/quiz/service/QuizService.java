package com.knupbackend.quiz.service;

import com.knupbackend.global.exception.ErrorCode;
import com.knupbackend.global.exception.KnupException;
import com.knupbackend.global.response.PageResponse;
import com.knupbackend.quiz.dto.request.QuizCreateRequest;
import com.knupbackend.quiz.dto.response.QuizDetailResponse;
import com.knupbackend.quiz.dto.response.QuizSummaryResponse;
import com.knupbackend.quiz.domain.Question;
import com.knupbackend.quiz.domain.Quiz;
import com.knupbackend.quiz.domain.QuizRepository;
import com.knupbackend.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;

    /** 퀴즈 생성 */
    @Transactional
    public QuizDetailResponse createQuiz(QuizCreateRequest request, User user) {
        Quiz quiz = Quiz.builder()
                .title(request.title())
                .description(request.description())
                .user(user)
                .build();

        quiz.updateQuestions(buildQuestions(request, quiz));
        return QuizDetailResponse.from(quizRepository.save(quiz));
    }

    /** 내 퀴즈 목록 (페이지네이션) */
    @Transactional(readOnly = true)
    public PageResponse<QuizSummaryResponse> getMyQuizzes(User user, int page, int size, String sort) {
        String field = "title".equals(sort) ? "title" : "createdAt";
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, field));
        return PageResponse.from(quizRepository.findByUser(user, pageable), QuizSummaryResponse::from);
    }

    /** 퀴즈 상세 조회 */
    @Transactional(readOnly = true)
    public QuizDetailResponse getQuiz(Long quizId, User user) {
        return QuizDetailResponse.from(findById(quizId));
    }

    /** 퀴즈 수정 */
    @Transactional
    public QuizDetailResponse updateQuiz(Long quizId, QuizCreateRequest request, User user) {
        Quiz quiz = findById(quizId);
        checkOwnership(quiz, user);

        quiz.update(request.title(), request.description());
        quiz.updateQuestions(buildQuestions(request, quiz));
        return QuizDetailResponse.from(quiz);
    }

    /** 퀴즈 삭제 */
    @Transactional
    public void deleteQuiz(Long quizId, User user) {
        Quiz quiz = findById(quizId);
        checkOwnership(quiz, user);
        quizRepository.delete(quiz);
    }

    // ── 내부 헬퍼 ─────────────────────────────────────────────────

    private Quiz findById(Long quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new KnupException(ErrorCode.QUIZ_NOT_FOUND));
    }

    private void checkOwnership(Quiz quiz, User user) {
        if (!quiz.isOwner(user.getId())) {
            throw new KnupException(ErrorCode.QUIZ_ACCESS_DENIED);
        }
    }

    private List<Question> buildQuestions(QuizCreateRequest request, Quiz quiz) {
        if (request.questions() == null) return new ArrayList<>();

        return IntStream.range(0, request.questions().size())
                .mapToObj(i -> {
                    var q = request.questions().get(i);
                    return Question.builder()
                            .quiz(quiz)
                            .content(q.content())
                            .questionType(q.questionType())
                            .options(q.options() != null ? q.options() : new ArrayList<>())
                            .correctAnswer(q.correctAnswer())
                            .timeLimit(q.timeLimit())
                            .points(q.points())
                            .orderIndex(i)
                            .build();
                })
                .toList();
    }
}
