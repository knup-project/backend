# ⚙️ KNU-P Backend · 크누피(QuizFlow) API

> 경북대 실시간 퀴즈 플랫폼 **크누피**의 백엔드. REST + WebSocket(STOMP) API,
> AI 퀴즈 생성, 실시간 게임 진행 엔진을 담당합니다.

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.14-6DB33F?logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white)
![Gemini](https://img.shields.io/badge/Google_Gemini-2.5_flash--lite-8E75B2?logo=googlegemini&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-green)
![last commit](https://img.shields.io/github/last-commit/knup-project/backend)

---

## 📌 개요

- **역할**: 호스트(로그인) 퀴즈 관리 + 참가자(무로그인) 실시간 게임 진행 + AI 퀴즈 생성
- **인증**: Spring Security **미사용** — 커스텀 `AuthInterceptor` + `HttpSession` 쿠키 + `@LoginUser`
- **실시간**: STOMP over WebSocket(`/ws`, SockJS), `/topic/session/{id}/…` 브로드캐스트
- **AI**: Google Gemini (`gemini-2.5-flash-lite`) — 텍스트/PDF → 퀴즈 생성, 해설
- **관측**: Actuator + Micrometer Prometheus (관리 포트 `:8081` 분리)

## 🛠 기술 스택

| 영역 | 사용 기술 |
|------|-----------|
| 프레임워크 | Spring Boot 3.5.14 |
| 언어 | Java 21 |
| 웹 | Spring Web · Validation · **WebSocket** |
| 영속성 | Spring Data JPA · MySQL 8 (런타임 H2) |
| 인증 | `spring-security-crypto`(BCrypt만) + 커스텀 인터셉터/세션 |
| AI | Google Gemini REST |
| 관측 | Actuator · `micrometer-registry-prometheus` |
| 빌드 | Gradle (Java toolchain 21) |

## 📁 패키지 구조 (도메인형)

```
com.knupbackend
├── global/
│   ├── auth/         # AuthInterceptor, @LoginUser, ArgumentResolver
│   ├── config/       # WebMvcConfig(CORS), WebSocketConfig(STOMP)
│   ├── common/       # BaseEntity(createdAt)
│   ├── exception/    # KnupException, GlobalExceptionHandler, ErrorCode
│   └── response/     # PageResponse
├── user/             # User 엔티티 + Repository
├── auth/             # 회원가입/로그인/세션 (presentation·service)
├── quiz/             # Quiz·Question CRUD
├── session/          # GameSession·Participant·Answer, 게임 진행·리더보드·정리 스케줄러
└── ai/               # Gemini 연동 (퀴즈 생성·해설)
```

## 🔌 API 요약

자세한 전수 명세는 **[docs/API.md](docs/API.md)** 참고. 베이스 경로는 `/api/v1`.

| 그룹 | 대표 엔드포인트 |
|------|------------------|
| **auth** | `POST /auth/signup` · `POST /auth/login` · `GET /auth/me` |
| **quizzes** | `POST /quizzes` · `GET /quizzes/me` · `GET·PUT·DELETE /quizzes/{id}` |
| **sessions** | `POST /sessions` · `POST /sessions/join` · `POST /sessions/{id}/start·next·end·answer` |
| **leaderboard** | `GET /sessions/{id}/leaderboard` · `/leaderboard/teams` · `/stats` |
| **ai** | `POST /ai/quiz/generate` · `/quiz/generate/pdf` · `/explain` |

실시간(WS): `/ws` 연결 → `/topic/session/{id}/{question|result|leaderboard|status|participants}` 구독.

## 🚀 로컬 실행

```bash
# 1) 빌드 + 테스트
./gradlew build

# 2) 실행 (기본 H2 런타임이면 별도 DB 없이 기동 가능)
./gradlew bootRun

# 3) 필요한 환경변수 (MySQL/Gemini 사용 시)
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/knup
export SPRING_DATASOURCE_USERNAME=...
export SPRING_DATASOURCE_PASSWORD=...
export GEMINI_API_KEY=...
```

| 포트 | 용도 |
|------|------|
| `8080` | 애플리케이션 (REST + WS) |
| `8081` | 관리(Actuator/Prometheus) — 외부 미노출 |

## 🐳 Docker

```bash
docker build -t ghcr.io/knup-project/backend .
docker run -p 8080:8080 -p 8081:8081 --env-file .env ghcr.io/knup-project/backend
```

CI(`.github/workflows/docker-image.yml`)가 `main` push 시 ghcr 이미지를 빌드·푸시하고,
[`infra`](https://github.com/knup-project/infra) 레포의 VM에 자동 배포합니다.

## 🧪 테스트

```bash
./gradlew test
```

`AuthServiceTest` · `QuizServiceTest` · `LeaderboardServiceTest` 등 서비스 단위 테스트 포함.

## 🔗 관련

- 📐 [구현 아키텍처](docs/ARCHITECTURE.md) · 🗺️ [로드맵](https://github.com/knup-project/.github/blob/main/docs/ROADMAP.md)
- 🎨 [frontend](https://github.com/knup-project/frontend) · ☁️ [infra](https://github.com/knup-project/infra)
- 🤝 [기여 가이드](https://github.com/knup-project/.github/blob/main/CONTRIBUTING.md)

## 📄 라이선스

[MIT](LICENSE) © 2026 knup-project team
