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

        assertThat(result.title()).isEqualTo("테스트 퀴즈");
        assertThat(result.questions()).hasSize(1);
        assertThat(quizRepository.findById(result.id())).isPresent();
    }

    @Test
    @DisplayName("내 퀴즈 목록 - 본인 퀴즈만 반환")
    void getMyQuizzes_success() {
        quizService.createQuiz(quizCreateRequest(), testUser);
        quizService.createQuiz(quizCreateRequest(), otherUser);

        PageResponse<QuizSummaryResponse> result =
                quizService.getMyQuizzes(testUser, 0, 20, "createdAt");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("테스트 퀴즈");
    }

    @Test
    @DisplayName("퀴즈 상세 조회 - 성공")
    void getQuiz_success() {
        QuizDetailResponse created = quizService.createQuiz(quizCreateRequest(), testUser);

        QuizDetailResponse result = quizService.getQuiz(created.id(), testUser);

        assertThat(result.id()).isEqualTo(created.id());
        assertThat(result.title()).isEqualTo("테스트 퀴즈");
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

        QuizCreateRequest updateReq = new QuizCreateRequest("수정된 퀴즈", quizCreateRequest().description(), quizCreateRequest().questions());

        QuizDetailResponse result = quizService.updateQuiz(created.id(), updateReq, testUser);

        assertThat(result.title()).isEqualTo("수정된 퀴즈");
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
                quizService.updateQuiz(created.id(), quizCreateRequest(), otherUser))
                .isInstanceOf(KnupException.class)
                .satisfies(e ->
                        assertThat(((KnupException) e).getErrorCode())
                                .isEqualTo(ErrorCode.QUIZ_ACCESS_DENIED));
    }

    @Test
    @DisplayName("퀴즈 삭제 - 성공 시 DB에서 제거")
    void deleteQuiz_success() {
        QuizDetailResponse created = quizService.createQuiz(quizCreateRequest(), testUser);

        quizService.deleteQuiz(created.id(), testUser);

        assertThat(quizRepository.findById(created.id())).isEmpty();
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
                quizService.deleteQuiz(created.id(), otherUser))
                .isInstanceOf(KnupException.class)
                .satisfies(e ->
                        assertThat(((KnupException) e).getErrorCode())
                                .isEqualTo(ErrorCode.QUIZ_ACCESS_DENIED));
    }

    private QuizCreateRequest quizCreateRequest() {
        QuestionRequest q = new QuestionRequest("1 + 1 = ?", QuestionType.SHORT_ANSWER, null, "2", 30, 10);
        return new QuizCreateRequest("테스트 퀴즈", "퀴즈 설명", List.of(q));
    }
}
