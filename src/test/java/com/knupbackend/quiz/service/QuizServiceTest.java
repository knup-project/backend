package com.knupbackend.quiz.service;

import com.knupbackend.global.exception.ErrorCode;
import com.knupbackend.global.exception.KnupException;
import com.knupbackend.global.response.PageResponse;
import com.knupbackend.quiz.domain.QuestionType;
import com.knupbackend.quiz.domain.QuizRepository;
import com.knupbackend.quiz.dto.request.QuestionRequest;
import com.knupbackend.quiz.dto.request.QuizCreateRequest;
import com.knupbackend.quiz.dto.response.QuizDetailResponse;
import com.knupbackend.quiz.dto.response.QuizSummaryResponse;
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
class QuizServiceTest {

    @Autowired QuizService quizService;
    @Autowired QuizRepository quizRepository;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = userRepository.save(User.builder()
                .email("test@test.com")
                .password(passwordEncoder.encode("password123"))
                .nickname("테스터")
                .build());

        otherUser = userRepository.save(User.builder()
                .email("other@test.com")
                .password(passwordEncoder.encode("password123"))
                .nickname("타인")
                .build());
    }

    @Test
    @DisplayName("퀴즈 생성 - 성공 시 DB에 저장 후 상세 반환")
    void createQuiz_success() {
        QuizDetailResponse result = quizService.createQuiz(quizCreateRequest(), testUser);

        assertThat(result.getTitle()).isEqualTo("테스트 퀴즈");
        assertThat(result.getQuestions()).hasSize(1);
        assertThat(quizRepository.findById(result.getId())).isPresent();
    }

    @Test
    @DisplayName("내 퀴즈 목록 - 본인 퀴즈만 반환")
    void getMyQuizzes_success() {
        quizService.createQuiz(quizCreateRequest(), testUser);
        quizService.createQuiz(quizCreateRequest(), otherUser);

        PageResponse<QuizSummaryResponse> result =
                quizService.getMyQuizzes(testUser, 0, 20, "createdAt");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("테스트 퀴즈");
    }

    @Test
    @DisplayName("퀴즈 상세 조회 - 성공")
    void getQuiz_success() {
        QuizDetailResponse created = quizService.createQuiz(quizCreateRequest(), testUser);

        QuizDetailResponse result = quizService.getQuiz(created.getId(), testUser);

        assertThat(result.getId()).isEqualTo(created.getId());
        assertThat(result.getTitle()).isEqualTo("테스트 퀴즈");
    }

    @Test
    @DisplayName("퀴즈 상세 조회 - 없는 퀴즈 → QUIZ_NOT_FOUND 예외")
    void getQuiz_notFound_throwsException() {
        assertThatThrownBy(() -> quizService.getQuiz(999L, testUser))
                .isInstanceOf(KnupException.class)
                .satisfies(e ->
                        assertThat(((KnupException) e).getErrorCode())
                                .isEqualTo(ErrorCode.QUIZ_NOT_FOUND));
    }

    @Test
    @DisplayName("퀴즈 수정 - 성공")
    void updateQuiz_success() {
        QuizDetailResponse created = quizService.createQuiz(quizCreateRequest(), testUser);

        QuizCreateRequest updateReq = quizCreateRequest();
        updateReq.setTitle("수정된 퀴즈");

        QuizDetailResponse result = quizService.updateQuiz(created.getId(), updateReq, testUser);

        assertThat(result.getTitle()).isEqualTo("수정된 퀴즈");
    }

    @Test
    @DisplayName("퀴즈 수정 - 없는 퀴즈 → QUIZ_NOT_FOUND 예외")
    void updateQuiz_notFound_throwsException() {
        assertThatThrownBy(() ->
                quizService.updateQuiz(999L, quizCreateRequest(), testUser))
                .isInstanceOf(KnupException.class)
                .satisfies(e ->
                        assertThat(((KnupException) e).getErrorCode())
                                .isEqualTo(ErrorCode.QUIZ_NOT_FOUND));
    }

    @Test
    @DisplayName("퀴즈 수정 - 본인 퀴즈 아님 → QUIZ_ACCESS_DENIED 예외")
    void updateQuiz_accessDenied_throwsException() {
        QuizDetailResponse created = quizService.createQuiz(quizCreateRequest(), testUser);

        assertThatThrownBy(() ->
                quizService.updateQuiz(created.getId(), quizCreateRequest(), otherUser))
                .isInstanceOf(KnupException.class)
                .satisfies(e ->
                        assertThat(((KnupException) e).getErrorCode())
                                .isEqualTo(ErrorCode.QUIZ_ACCESS_DENIED));
    }

    @Test
    @DisplayName("퀴즈 삭제 - 성공 시 DB에서 제거")
    void deleteQuiz_success() {
        QuizDetailResponse created = quizService.createQuiz(quizCreateRequest(), testUser);

        quizService.deleteQuiz(created.getId(), testUser);

        assertThat(quizRepository.findById(created.getId())).isEmpty();
    }

    @Test
    @DisplayName("퀴즈 삭제 - 없는 퀴즈 → QUIZ_NOT_FOUND 예외")
    void deleteQuiz_notFound_throwsException() {
        assertThatThrownBy(() -> quizService.deleteQuiz(999L, testUser))
                .isInstanceOf(KnupException.class)
                .satisfies(e ->
                        assertThat(((KnupException) e).getErrorCode())
                                .isEqualTo(ErrorCode.QUIZ_NOT_FOUND));
    }

    @Test
    @DisplayName("퀴즈 삭제 - 본인 퀴즈 아님 → QUIZ_ACCESS_DENIED 예외")
    void deleteQuiz_accessDenied_throwsException() {
        QuizDetailResponse created = quizService.createQuiz(quizCreateRequest(), testUser);

        assertThatThrownBy(() ->
                quizService.deleteQuiz(created.getId(), otherUser))
                .isInstanceOf(KnupException.class)
                .satisfies(e ->
                        assertThat(((KnupException) e).getErrorCode())
                                .isEqualTo(ErrorCode.QUIZ_ACCESS_DENIED));
    }

    private QuizCreateRequest quizCreateRequest() {
        QuestionRequest q = new QuestionRequest();
        q.setContent("1 + 1 = ?");
        q.setQuestionType(QuestionType.SHORT_ANSWER);
        q.setCorrectAnswer("2");
        q.setTimeLimit(30);
        q.setPoints(10);

        QuizCreateRequest req = new QuizCreateRequest();
        req.setTitle("테스트 퀴즈");
        req.setDescription("퀴즈 설명");
        req.setQuestions(List.of(q));
        return req;
    }
}
