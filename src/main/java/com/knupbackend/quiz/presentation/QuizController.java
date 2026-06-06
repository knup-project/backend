package com.knupbackend.quiz.presentation;

import com.knupbackend.global.auth.LoginUser;
import com.knupbackend.global.response.PageResponse;
import com.knupbackend.quiz.dto.request.QuizCreateRequest;
import com.knupbackend.quiz.dto.response.QuizDetailResponse;
import com.knupbackend.quiz.dto.response.QuizSummaryResponse;
import com.knupbackend.quiz.service.QuizService;
import com.knupbackend.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    /** POST /api/v1/quizzes — 퀴즈 생성 */
    @PostMapping
    public ResponseEntity<QuizDetailResponse> createQuiz(
            @Valid @RequestBody QuizCreateRequest request,
            @LoginUser User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(quizService.createQuiz(request, user));
    }

    /** GET /api/v1/quizzes/me — 내 퀴즈 목록 (리터럴 경로라 /{quizId}보다 우선 매칭) */
    @GetMapping("/me")
    public ResponseEntity<PageResponse<QuizSummaryResponse>> getMyQuizzes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @LoginUser User user) {
        return ResponseEntity.ok(quizService.getMyQuizzes(user, page, size, sort));
    }

    /** GET /api/v1/quizzes/{quizId} — 퀴즈 상세 조회 */
    @GetMapping("/{quizId}")
    public ResponseEntity<QuizDetailResponse> getQuiz(
            @PathVariable Long quizId,
            @LoginUser User user) {
        return ResponseEntity.ok(quizService.getQuiz(quizId, user));
    }

    /** PUT /api/v1/quizzes/{quizId} — 퀴즈 수정 */
    @PutMapping("/{quizId}")
    public ResponseEntity<QuizDetailResponse> updateQuiz(
            @PathVariable Long quizId,
            @Valid @RequestBody QuizCreateRequest request,
            @LoginUser User user) {
        return ResponseEntity.ok(quizService.updateQuiz(quizId, request, user));
    }

    /** DELETE /api/v1/quizzes/{quizId} — 퀴즈 삭제 */
    @DeleteMapping("/{quizId}")
    public ResponseEntity<Void> deleteQuiz(
            @PathVariable Long quizId,
            @LoginUser User user) {
        quizService.deleteQuiz(quizId, user);
        return ResponseEntity.noContent().build();
    }
}
