package com.knupbackend.quiz.service;

import com.knupbackend.global.exception.ErrorCode;
import com.knupbackend.global.exception.KnupException;
import com.knupbackend.global.response.PageResponse;
import com.knupbackend.quiz.dto.request.QuestionRequest;
import com.knupbackend.quiz.dto.request.QuizCreateRequest;
import com.knupbackend.quiz.dto.response.QuizDetailResponse;
import com.knupbackend.quiz.dto.response.QuizSummaryResponse;
import com.knupbackend.quiz.entity.Question;
import com.knupbackend.quiz.entity.Quiz;
import com.knupbackend.quiz.entity.QuestionType;
import com.knupbackend.quiz.repository.QuizRepository;
import com.knupbackend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock private QuizRepository quizRepository;

    @InjectMocks private QuizService quizService;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L).email("test@test.com").password("encoded")
                .nickname("테스터").createdAt(LocalDateTime.now()).build();

        otherUser = User.builder()
                .id(2L).email("other@test.com").password("encoded")
                .nickname("타인").createdAt(LocalDateTime.now()).build();
    }

    // ══════════════════════════════════════════════════════════════
    // 퀴즈 생성
    // ══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("퀴즈 생성 - 성공 시 저장 후 상세 반환")
    void createQuiz_success() {
        given(quizRepository.save(any())).willReturn(savedQuiz());

        QuizDetailResponse result = quizService.createQuiz(quizCreateRequest(), testUser);

        assertThat(result.getTitle()).isEqualTo("테스트 퀴즈");
        assertThat(result.getQuestions()).hasSize(1);
        verify(quizRepository).save(any(Quiz.class));
    }

    // ══════════════════════════════════════════════════════════════
    // 내 퀴즈 목록
    // ══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("내 퀴즈 목록 - 성공 시 페이지네이션 반환")
    void getMyQuizzes_success() {
        given(quizRepository.findByUser(any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(savedQuiz())));

        PageResponse<QuizSummaryResponse> result =
                quizService.getMyQuizzes(testUser, 0, 20, "createdAt");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("테스트 퀴즈");
    }

    // ══════════════════════════════════════════════════════════════
    // 퀴즈 상세 조회
    // ══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("퀴즈 상세 조회 - 성공")
    void getQuiz_success() {
        given(quizRepository.findById(1L)).willReturn(Optional.of(savedQuiz()));

        QuizDetailResponse result = quizService.getQuiz(1L, testUser);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("테스트 퀴즈");
    }

    @Test
    @DisplayName("퀴즈 상세 조회 - 없는 퀴즈 → QUIZ_NOT_FOUND 예외")
    void getQuiz_notFound_throwsException() {
        given(quizRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> quizService.getQuiz(99L, testUser))
                .isInstanceOf(KnupException.class)
                .satisfies(e ->
                        assertThat(((KnupException) e).getErrorCode())
                                .isEqualTo(ErrorCode.QUIZ_NOT_FOUND));
    }

    // ══════════════════════════════════════════════════════════════
    // 퀴즈 수정
    // ══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("퀴즈 수정 - 성공")
    void updateQuiz_success() {
        given(quizRepository.findById(1L)).willReturn(Optional.of(savedQuiz()));

        QuizCreateRequest updateReq = quizCreateRequest();
        updateReq.setTitle("수정된 퀴즈");

        QuizDetailResponse result = quizService.updateQuiz(1L, updateReq, testUser);

        assertThat(result.getTitle()).isEqualTo("수정된 퀴즈");
    }

    @Test
    @DisplayName("퀴즈 수정 - 없는 퀴즈 → QUIZ_NOT_FOUND 예외")
    void updateQuiz_notFound_throwsException() {
        given(quizRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> quizService.updateQuiz(99L, quizCreateRequest(), testUser))
                .isInstanceOf(KnupException.class)
                .satisfies(e ->
                        assertThat(((KnupException) e).getErrorCode())
                                .isEqualTo(ErrorCode.QUIZ_NOT_FOUND));
    }

    @Test
    @DisplayName("퀴즈 수정 - 본인 퀴즈 아님 → QUIZ_ACCESS_DENIED 예외")
    void updateQuiz_accessDenied_throwsException() {
        given(quizRepository.findById(1L)).willReturn(Optional.of(savedQuiz()));

        assertThatThrownBy(() -> quizService.updateQuiz(1L, quizCreateRequest(), otherUser))
                .isInstanceOf(KnupException.class)
                .satisfies(e ->
                        assertThat(((KnupException) e).getErrorCode())
                                .isEqualTo(ErrorCode.QUIZ_ACCESS_DENIED));
    }

    // ══════════════════════════════════════════════════════════════
    // 퀴즈 삭제
    // ══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("퀴즈 삭제 - 성공")
    void deleteQuiz_success() {
        Quiz quiz = savedQuiz();
        given(quizRepository.findById(1L)).willReturn(Optional.of(quiz));

        quizService.deleteQuiz(1L, testUser);

        verify(quizRepository).delete(quiz);
    }

    @Test
    @DisplayName("퀴즈 삭제 - 없는 퀴즈 → QUIZ_NOT_FOUND 예외")
    void deleteQuiz_notFound_throwsException() {
        given(quizRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> quizService.deleteQuiz(99L, testUser))
                .isInstanceOf(KnupException.class)
                .satisfies(e ->
                        assertThat(((KnupException) e).getErrorCode())
                                .isEqualTo(ErrorCode.QUIZ_NOT_FOUND));
    }

    @Test
    @DisplayName("퀴즈 삭제 - 본인 퀴즈 아님 → QUIZ_ACCESS_DENIED 예외")
    void deleteQuiz_accessDenied_throwsException() {
        given(quizRepository.findById(1L)).willReturn(Optional.of(savedQuiz()));

        assertThatThrownBy(() -> quizService.deleteQuiz(1L, otherUser))
                .isInstanceOf(KnupException.class)
                .satisfies(e ->
                        assertThat(((KnupException) e).getErrorCode())
                                .isEqualTo(ErrorCode.QUIZ_ACCESS_DENIED));
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────

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

    private Quiz savedQuiz() {
        Quiz quiz = Quiz.builder()
                .id(1L)
                .title("테스트 퀴즈")
                .description("퀴즈 설명")
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Question question = Question.builder()
                .id(1L).quiz(quiz).content("1 + 1 = ?")
                .questionType(QuestionType.SHORT_ANSWER)
                .correctAnswer("2").timeLimit(30).points(10).orderIndex(0)
                .build();

        quiz.getQuestions().add(question);
        return quiz;
    }
}
