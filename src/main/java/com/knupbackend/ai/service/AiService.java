package com.knupbackend.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knupbackend.ai.dto.request.AiExplainRequest;
import com.knupbackend.ai.dto.request.AiQuizGenerateRequest;
import com.knupbackend.ai.dto.response.AiExplainResponse;
import com.knupbackend.ai.dto.response.AiQuizGenerateResponse;
import com.knupbackend.global.exception.ErrorCode;
import com.knupbackend.global.exception.KnupException;
import com.knupbackend.quiz.domain.Question;
import com.knupbackend.quiz.domain.QuestionRepository;
import com.knupbackend.quiz.domain.QuestionType;
import com.knupbackend.quiz.dto.request.QuestionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    /** Gemini inline_data 한계(요청 전체 ~20MB, base64 +33%)를 넘지 않는 실효 상한 */
    private static final long MAX_PDF_BYTES = 15L * 1024 * 1024;

    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient = createRestClient();

    /** FE가 먼저 포기해도 톰캣 스레드가 무기한 붙잡히지 않도록 연결/응답 타임아웃을 둔다. */
    private static RestClient createRestClient() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);
        factory.setReadTimeout(60_000);
        return RestClient.builder().requestFactory(factory).build();
    }

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String model;

    public AiQuizGenerateResponse generateFromText(AiQuizGenerateRequest request) {
        String prompt = quizPrompt(request.text(), request.questionCount(),
                request.questionType(), request.difficulty(), request.language());
        String json = callGemini(List.of(Map.of("text", prompt)));
        return new AiQuizGenerateResponse(parseQuestions(json), now());
    }

    public AiQuizGenerateResponse generateFromPdf(MultipartFile file, Integer questionCount,
                                                  String questionType, String difficulty) {
        if (file == null || file.isEmpty()) {
            throw new KnupException(ErrorCode.INVALID_INPUT);
        }
        if (file.getSize() > MAX_PDF_BYTES) {
            throw new KnupException(ErrorCode.PDF_TOO_LARGE);
        }
        String base64;
        try {
            base64 = Base64.getEncoder().encodeToString(file.getBytes());
        } catch (Exception e) {
            throw new KnupException(ErrorCode.INVALID_INPUT);
        }
        String prompt = quizPrompt("(첨부된 PDF 문서의 내용)", questionCount, questionType, difficulty, null);
        List<Map<String, Object>> parts = List.of(
                Map.of("inline_data", Map.of("mime_type", "application/pdf", "data", base64)),
                Map.of("text", prompt)
        );
        String json = callGemini(parts);
        return new AiQuizGenerateResponse(parseQuestions(json), now());
    }

    public AiExplainResponse explain(AiExplainRequest request) {
        Question q = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new KnupException(ErrorCode.QUESTION_NOT_FOUND));
        String prompt = explainPrompt(q, request.participantAnswer());
        String json = callGemini(List.of(Map.of("text", prompt)));
        try {
            JsonNode node = objectMapper.readTree(stripFences(json));
            return new AiExplainResponse(
                    node.path("explanation").asText(""),
                    node.hasNonNull("hint") ? node.path("hint").asText() : null,
                    now()
            );
        } catch (Exception e) {
            log.warn("AI explain parse failed: {}", e.getMessage());
            throw new KnupException(ErrorCode.AI_SERVICE_ERROR);
        }
    }

    // ── Gemini call ───────────────────────────────────────────────

    private String callGemini(List<? extends Map<String, Object>> parts) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY is not configured");
            throw new KnupException(ErrorCode.AI_SERVICE_ERROR);
        }
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", parts)),
                "generationConfig", Map.of("responseMimeType", "application/json", "temperature", 0.7)
        );
        try {
            String response = restClient.post()
                    .uri(BASE_URL + model + ":generateContent?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(response);
            return root.path("candidates").path(0).path("content")
                    .path("parts").path(0).path("text").asText("");
        } catch (RestClientResponseException e) {
            log.warn("Gemini API error {}: {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 429) {
                throw new KnupException(ErrorCode.AI_RATE_LIMITED);
            }
            throw new KnupException(ErrorCode.AI_SERVICE_ERROR);
        } catch (KnupException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Gemini call failed: {}", e.getMessage());
            throw new KnupException(ErrorCode.AI_SERVICE_ERROR);
        }
    }

    private List<QuestionRequest> parseQuestions(String json) {
        try {
            JsonNode root = objectMapper.readTree(stripFences(json));
            JsonNode arr = root.isArray() ? root : root.path("questions");
            List<QuestionRequest> out = new ArrayList<>();
            for (JsonNode n : arr) {
                List<String> options = new ArrayList<>();
                if (n.has("options") && n.get("options").isArray()) {
                    n.get("options").forEach(o -> options.add(o.asText()));
                }
                out.add(new QuestionRequest(
                        n.path("content").asText(""),
                        parseType(n.path("type").asText("MULTIPLE_CHOICE")),
                        options,
                        normalizeAnswer(n.path("answer").asText(""), options),
                        n.hasNonNull("explanation") ? n.path("explanation").asText() : null,
                        n.has("timeLimit") ? n.path("timeLimit").asInt(20) : 20,
                        n.has("points") ? n.path("points").asInt(100) : 100
                ));
            }
            if (out.isEmpty()) {
                throw new KnupException(ErrorCode.AI_SERVICE_ERROR);
            }
            return out;
        } catch (KnupException e) {
            throw e;
        } catch (Exception e) {
            log.warn("AI quiz parse failed: {}", e.getMessage());
            throw new KnupException(ErrorCode.AI_SERVICE_ERROR);
        }
    }

    /** 모델이 프롬프트를 어겨 ```json 펜스로 감싸 보낸 경우를 방어한다. */
    private String stripFences(String s) {
        String t = s == null ? "" : s.trim();
        if (t.startsWith("```")) {
            t = t.replaceFirst("^```[a-zA-Z]*\\s*", "").replaceFirst("\\s*```$", "").trim();
        }
        return t;
    }

    /** 정답이 옵션과 대소문자/공백만 다르면 옵션 표기로 맞춘다(프론트는 exact match로 정답 표시). */
    private String normalizeAnswer(String answer, List<String> options) {
        if (answer == null) return "";
        String trimmed = answer.trim();
        for (String opt : options) {
            if (opt != null && opt.trim().equalsIgnoreCase(trimmed)) {
                return opt;
            }
        }
        return trimmed;
    }

    private QuestionType parseType(String s) {
        try {
            return QuestionType.valueOf(s.trim().toUpperCase());
        } catch (Exception e) {
            return QuestionType.MULTIPLE_CHOICE;
        }
    }

    private String quizPrompt(String source, Integer count, String type, String difficulty, String language) {
        int n = (count != null && count > 0) ? count : 5;
        String lang = "EN".equalsIgnoreCase(language) ? "English" : "Korean";
        String typeHint = switch (type == null ? "MIXED" : type.toUpperCase()) {
            case "MULTIPLE_CHOICE" -> "Use only MULTIPLE_CHOICE questions.";
            case "TRUE_FALSE" -> "Use only TRUE_FALSE questions.";
            default -> "Mix MULTIPLE_CHOICE and TRUE_FALSE questions.";
        };
        String diff = (difficulty == null) ? "MEDIUM" : difficulty.toUpperCase();
        return """
                You are a quiz generator. Based on the material below, create %d quiz questions (difficulty: %s). %s
                Respond with ONLY a JSON object, no markdown, in this exact schema:
                {"questions":[{"content":"...","type":"MULTIPLE_CHOICE|TRUE_FALSE|SHORT_ANSWER","options":["..."],"answer":"...","explanation":"...","timeLimit":20,"points":100}]}
                Rules: For MULTIPLE_CHOICE provide exactly 4 options and "answer" must be exactly equal to one of the options.
                For TRUE_FALSE use options ["참","거짓"] and answer one of them. For SHORT_ANSWER omit options.
                Write all text in %s. timeLimit in seconds (default 20), points default 100.
                Material:
                %s
                """.formatted(n, diff, typeHint, lang, source);
    }

    private String explainPrompt(Question q, String participantAnswer) {
        String given = (participantAnswer == null || participantAnswer.isBlank())
                ? "(미응답)" : participantAnswer;
        return """
                You are a tutor. Respond with ONLY a JSON object, no markdown, in this schema:
                {"explanation":"...","hint":"..."}
                Write in Korean. Explain clearly why the correct answer is right (and, if the participant's answer differs, why it is wrong). "hint" is one short helpful hint.
                Question: %s
                Correct answer: %s
                Participant's answer: %s
                """.formatted(q.getContent(), String.valueOf(q.getCorrectAnswer()), given);
    }

    private String now() {
        return LocalDateTime.now().toString();
    }
}
