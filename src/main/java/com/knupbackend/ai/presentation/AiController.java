package com.knupbackend.ai.presentation;

import com.knupbackend.ai.dto.request.AiExplainRequest;
import com.knupbackend.ai.dto.request.AiQuizGenerateRequest;
import com.knupbackend.ai.dto.response.AiExplainResponse;
import com.knupbackend.ai.dto.response.AiQuizGenerateResponse;
import com.knupbackend.ai.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /** 텍스트 기반 AI 퀴즈 생성 */
    @PostMapping("/quiz/generate")
    public ResponseEntity<AiQuizGenerateResponse> generateFromText(@Valid @RequestBody AiQuizGenerateRequest request) {
        return ResponseEntity.ok(aiService.generateFromText(request));
    }

    /** PDF 기반 AI 퀴즈 생성 (멀티파트) */
    @PostMapping(value = "/quiz/generate/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AiQuizGenerateResponse> generateFromPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "questionCount", required = false) Integer questionCount,
            @RequestParam(value = "questionType", required = false) String questionType,
            @RequestParam(value = "difficulty", required = false) String difficulty) {
        return ResponseEntity.ok(aiService.generateFromPdf(file, questionCount, questionType, difficulty));
    }

    /** AI 해설 생성 */
    @PostMapping("/explain")
    public ResponseEntity<AiExplainResponse> explain(@Valid @RequestBody AiExplainRequest request) {
        return ResponseEntity.ok(aiService.explain(request));
    }
}
