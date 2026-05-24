package com.knupbackend.quiz.dto.request;

import com.knupbackend.quiz.entity.QuestionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class QuestionRequest {

    @NotBlank(message = "문제 내용은 필수입니다.")
    private String content;

    @NotNull(message = "문제 유형은 필수입니다.")
    private QuestionType questionType;

    private List<String> options;
    private String correctAnswer;

    @Min(value = 5, message = "제한 시간은 최소 5초 이상이어야 합니다.")
    private Integer timeLimit;

    @Min(value = 1, message = "점수는 최소 1점 이상이어야 합니다.")
    private Integer points;
}
