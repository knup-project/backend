# 📚 API Reference — 크누피(QuizFlow) Backend

베이스 URL: `https://144-24-92-17.sslip.io/api/v1` (로컬: `http://localhost:8080/api/v1`)

- **인증**: `HttpSession` 쿠키(`JSESSIONID`, httpOnly). 로그인 후 쿠키 자동 전송(`withCredentials`).
- **공개(인증 불필요)**: `auth/signup`, `auth/login`, `sessions/join`, `GET sessions/{id}`,
  `sessions/{id}/answer`, `sessions/{id}/leaderboard`, `sessions/{id}/leaderboard/teams`, CORS preflight.
- **세션**: 인메모리, timeout 30분, 쿠키 `SameSite=None; Secure`.

---

## 🔐 Auth — `/api/v1/auth`

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| POST | `/auth/signup` | 회원가입 (email/password/nickname) | ❌ |
| POST | `/auth/login` | 로그인 → 세션 쿠키 발급 | ❌ |
| POST | `/auth/logout` | 로그아웃 (세션 무효화) | ✅ |
| GET | `/auth/me` | 현재 로그인 사용자 조회 | ✅ |

## 📝 Quizzes — `/api/v1/quizzes`

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| POST | `/quizzes` | 퀴즈 생성 (Question 포함) | ✅ |
| GET | `/quizzes/me` | 내 퀴즈 목록 | ✅ |
| GET | `/quizzes/{id}` | 퀴즈 상세 | ✅ |
| PUT | `/quizzes/{id}` | 퀴즈 수정 | ✅ |
| DELETE | `/quizzes/{id}` | 퀴즈 삭제 | ✅ |

`Question.type`: `MULTIPLE_CHOICE` · `TRUE_FALSE` · `SHORT_ANSWER` (각 `options`/`answer`/`timeLimit`/`points`).

## 🎮 Sessions — `/api/v1/sessions`

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| POST | `/sessions` | 세션 개설 (quizId, mode) → PIN 발급 | ✅ |
| GET | `/sessions/{id}` | 세션 조회 (참가자 목록 포함) | ❌ |
| POST | `/sessions/join` | PIN + 닉네임으로 참가 → `participantId` | ❌ |
| POST | `/sessions/{id}/start` | 세션 시작 | ✅ |
| POST | `/sessions/{id}/next` | 다음 문제로 진행 | ✅ |
| POST | `/sessions/{id}/end` | 세션 종료 | ✅ |
| POST | `/sessions/{id}/answer` | 답안 제출 (헤더 `X-Participant-Id`) | ❌ |
| DELETE | `/sessions/{id}/participants/{pid}` | 참가자 강퇴(단건) | ✅ |
| POST | `/sessions/{id}/participants/kick` | 일괄/전체 강퇴(빈 배열=전체) | ✅ |

`mode`: `INDIVIDUAL` · `TEAM` / `status`: `WAITING` · `IN_PROGRESS` · `FINISHED`.
응답 `SessionResponse`는 `sessionId` + `participants[]` 포함, 팀 식별자 `teamId = teamName`.

## 🏆 Leaderboard — `/api/v1/sessions/{id}`

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| GET | `/leaderboard` | 개인 리더보드 | ❌ |
| GET | `/leaderboard/teams` | 팀 리더보드 | ❌ |
| GET | `/stats` | 세션 통계 | ✅ |

점수는 정답 여부 + 응답 속도(`responseTimeSec`) 가중.

## 🤖 AI — `/api/v1/ai`

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| POST | `/ai/quiz/generate` | 텍스트 → 퀴즈 생성 | ✅ |
| POST | `/ai/quiz/generate/pdf` | PDF(multipart) → 퀴즈 생성 | ✅ |
| POST | `/ai/explain` | 문제 해설 생성 | ✅ |

엔진: Google Gemini `gemini-2.5-flash-lite`.

---

## 📡 WebSocket (STOMP)

- 연결: `wss://144-24-92-17.sslip.io/ws` (SockJS), prefix `/app`, broker `/topic`.
- **구독 (서버 → 클라)** `/topic/session/{id}/…`: `question` · `result` · `leaderboard` · `status` · `participants`
- **발행 (클라 → 서버)** `/app/session/{id}/…`: `heartbeat`(30s)
- 답안은 신뢰성을 위해 **REST `POST /answer`** 로 전송하며, WS는 수신·하트비트 용도.

## ⚠️ 에러 응답

```json
{ "code": "QUIZ_NOT_FOUND", "message": "..." }
```
`GlobalExceptionHandler` + `ErrorCode`로 일관된 형식 반환.

---
<sub>상세 동작/제약은 <a href="ARCHITECTURE.md">ARCHITECTURE.md</a> 참고.</sub>
