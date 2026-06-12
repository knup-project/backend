# 크누피 (QuizFlow / KNU-P) — 실제 구현 아키텍처

> **현재 레포 코드·배포 상태 기준**의 실제 구조 문서다. 기획·제안서(`QuizFlow_프로젝트_제안서.md`,
> `repos/frontend/docs/DESIGN.md`)와 **다른 점은 §13에 명시**한다. (기준일: 2026-06-09)

---

## 1. 한눈에

- **서비스**: 경북대학교 실시간 퀴즈 플랫폼 **"크누피"**. 호스트(교수/강사, 로그인) + 참가자(학생, **무로그인** PIN 입장).
- **3 레포** (GitHub org `knup-project`): `frontend`, `backend`, `infra`.
- **라이브**: 프론트 `https://158-180-94-80.sslip.io` · 백엔드 `https://144-24-92-17.sslip.io`.
- **클라우드**: Oracle Cloud Always Free VM **2대**(AMD) + 외부 SaaS(Google Gemini, Grafana Cloud).

## 2. 시스템 토폴로지

```
                       ┌──────────── OCI Always Free · ap-chuncheon-1 ────────────┐
                       │                                                          │
  브라우저 ──HTTPS────▶ Frontend VM  (E2.1.Micro 1c/1GB, 158-180-94-80)            │
 (호스트 데스크톱 /     │   nginx :443  ──▶  Next.js 16 standalone (Docker)         │
  참가자 모바일)        │                                                          │
       │               │                                                          │
       ├─REST(/api/v1)─▶ Backend VM   (E2.1.Micro 1c/1GB +2GB swap, 144-24-92-17)  │
       ├─WSS(/ws)──────▶   nginx :443 ──▶ Spring Boot :8080 (Docker)               │
       │               │                      │  └─ :8081 actuator (내부 전용)     │
       │               │                      ▼                          │        │
       │               │                  MySQL 8 (Docker + volume)       │        │
       │               │                                                  ▼        │
       │               │                              Grafana Alloy (Docker) ──┐   │
       └───────────────┘                                                       │   │
                         Google Gemini API (외부) ◀── AI 퀴즈 생성/해설          │   │
                         Grafana Cloud (외부·무료) ◀── remote_write (메트릭) ────┘   │
                       └──────────────────────────────────────────────────────────┘
```

## 3. 레포지토리 & 실제 스택

| 레포 | 핵심 스택 (실측) |
|---|---|
| **frontend** | Next.js **16.2.6** (App Router) · React **19.2.4** · TypeScript strict · Tailwind **v4** · `motion`(framer-motion) **12** · Zustand **5**(persist) · TanStack Query **v5** · Axios · `@stomp/stompjs` + `sockjs-client` · pnpm 10 / Node 22 |
| **backend** | Spring Boot **3.5.14** · Java **21** · Spring Web/Validation/**WebSocket**/Data JPA · **Spring Security 미사용**(`spring-security-crypto`의 BCrypt만) · MySQL(+H2 런타임) · Actuator + `micrometer-registry-prometheus` · Gradle |
| **infra** | **OpenTofu**(Terraform 호환) · OCI provider · 원격 state = OCI Object Storage(S3 호환) · Docker Compose · nginx · Let's Encrypt(certbot) · GitHub Actions |

## 4. 프론트엔드 (`frontend`)

- **구조(FSD)**: `app/`(라우트) · `features/{auth,quizzes,sessions,participants,leaderboard,ai}` · `shared/{api,ui,lib,constants,types}`.
- **라우트 (15)**:
  - 공개: `/` (랜딩) · `/join` (PIN 입장) · `/login` · `/signup`
  - 참가자 라이브: `/play/[sessionId]/{waiting,question,result,leaderboard}`
  - 호스트 대시보드: `/dashboard/quizzes` · `/dashboard/quizzes/new` · `/dashboard/quizzes/[quizId]/edit` · `/dashboard/ai-generate`
  - 호스트 세션: `/host/sessions/new` · `/host/sessions/[sessionId]/{waiting,play,result}`
- **인증/가드**: 세션 쿠키(JSESSIONID, httpOnly) + Axios `withCredentials`. `AuthGuard`(호스트 페이지 보호, `GET /auth/me` 검증) · `GuestGuard`(이미 로그인 시 `/login·/signup`→대시보드) · 401 인터셉터(참가자 경로 `GET /sessions/{id}`·answer·leaderboard·join은 `/login` 리다이렉트 예외).
- **상태/통신**: 서버상태 TanStack Query, 클라이언트 Zustand persist(`auth`, `participant`), 실시간 `useSessionSocket`(STOMP/SockJS).
- **디자인 시스템**: "Warm Arcade" — KNU Red `#E60000`/Gray/Gold 토큰, 다크 무대(`stage`), Neubrutalism 악센트(하드 섀도·`Jua` 디스플레이), 마스코트 **크누피**(SVG), 게임 모션 3종(`CountdownRing`/`CountUp`/`Celebration`), 4지선다 파/노/초/보라(레드 제외)+도형, `prefers-reduced-motion` 대응.
- **에러 바운더리**: `app/error.tsx`(전역) · `app/play/error.tsx`(라이브 다크).
- **빌드 주입 env**: `NEXT_PUBLIC_API_BASE_URL=https://144-24-92-17.sslip.io/api/v1`, `NEXT_PUBLIC_WS_URL=wss://144-24-92-17.sslip.io/ws`.

## 5. 백엔드 (`backend`)

- **인증 (Spring Security 아님)**: 커스텀 **`AuthInterceptor`**(HandlerInterceptor) + `HttpSession`(`LOGIN_USER_ID`) + `@LoginUser` ArgumentResolver.
  - `/api/v1/**` 전체 적용, **공개(exclude)**: `auth/signup`, `auth/login`, `sessions/join`, `sessions/*`(GET 세션 조회), `sessions/*/answer`, `sessions/*/leaderboard`, `sessions/*/leaderboard/teams`, + CORS preflight(OPTIONS).
  - 세션: 인메모리, `timeout=30m`, 쿠키 `SameSite=None; Secure`, `forward-headers-strategy=framework`.
  - CORS: `WebMvcConfig.addCorsMappings` (allowedOrigins = 프론트 158 / localhost:3000, `allowCredentials`).
- **REST 엔드포인트 (전수)**:
  - **auth** `/api/v1/auth`: `POST signup` · `POST login` · `POST logout` · `GET me`
  - **quizzes** `/api/v1/quizzes`: `POST /` · `GET /me` · `GET /{id}` · `PUT /{id}` · `DELETE /{id}`
  - **sessions** `/api/v1/sessions`: `POST /` · `GET /{id}` · `POST /join` · `POST /{id}/start` · `POST /{id}/next` · `POST /{id}/end` · `POST /{id}/answer`(헤더 `X-Participant-Id`) · `DELETE /{id}/participants/{pid}` · `POST /{id}/participants/kick`(빈 배열=전체)
  - **leaderboard** `/api/v1/sessions/{id}`: `GET /leaderboard` · `GET /leaderboard/teams` · `GET /stats`
  - **ai** `/api/v1/ai`: `POST /quiz/generate`(텍스트) · `POST /quiz/generate/pdf`(multipart) · `POST /explain`
- **AI**: Google Gemini (`gemini-2.5-flash-lite`) — 텍스트/PDF→퀴즈 생성, 해설.
- **운영 자동화**: `SessionCleanupScheduler`(`@Scheduled` 10분) → 생성 3시간↑ 미종료 세션 자동 종료(유령 세션 방지); 단건/일괄/전체 강퇴.
- **관측**: Actuator + Micrometer Prometheus, **`management.server.port=8081`**(메인 8080과 분리 → 외부 미노출).

## 6. 실시간 (WebSocket / STOMP)

- **엔드포인트**: `/ws` (SockJS), `setApplicationDestinationPrefixes("/app")`, `enableSimpleBroker("/topic")`, allowed origins = 158/144/localhost. **인증 없음**(참가자 무로그인 설계).
- **서버 → 클라 (`/topic/session/{id}/…`)**: `question` · `result` · `leaderboard` · `status` · `participants`.
- **클라 → 서버 (`/app/session/{id}/…`)**: `heartbeat`(30s). *답안은 신뢰성 위해 REST `POST /answer`로 전송*, WS는 수신·하트비트.
- **흐름**: PIN 입장(`POST join`) → 대기실(`GET session` + WS status) → 문제 수신(WS question) → 답안(`POST answer`) → 개인 결과 → `status=FINISHED` 시 리더보드.

## 7. 데이터 모델 (JPA 엔티티)

```
User 1───N Quiz 1───N Question
                │
GameSession N───1 Quiz
GameSession 1───N Participant 1───N Answer N───1 Question
(BaseEntity: createdAt @CreatedDate)
```
- **User**(email/password BCrypt/nickname) · **Quiz**(title/description, N Question) · **Question**(content/type[MULTIPLE_CHOICE·TRUE_FALSE·SHORT_ANSWER]/options/answer/timeLimit/points)
- **GameSession**(sessionId UUID·pin·mode[INDIVIDUAL·TEAM]·status[WAITING·IN_PROGRESS·FINISHED]·currentQuestionIndex·maxParticipants) · **Participant**(participantId UUID·nickname·teamName·score) · **Answer**(selectedAnswer·correct·responseTimeSec)
- 응답 계약: `SessionResponse`에 `sessionId`+`participants[]` 포함, 팀 식별자 `teamId = teamName`(통일).

## 8. 인프라 (`infra`, OpenTofu / OCI Always Free)

- **VM 2대** (둘 다 `VM.Standard.E2.1.Micro` = AMD 1 OCPU / 1 GB + 2 GB swap):
  - `app` = 백엔드(144) · `frontend` = 프론트(158). *(ARM A1 4c/24GB가 1순위였으나 ap-chuncheon-1 capacity 부재로 AMD 2대)*
- **네트워크**: VCN `10.0.0.0/16` + public subnet + IGW + Security List(ingress 22/80/443).
- **VM 부트(cloud-init, arch-agnostic)**: 2GB swap, Docker+Compose, nginx, certbot, iptables(80/443).
- **백엔드 VM 스택**(`vm/docker-compose.yml`): `backend`(ghcr 이미지, loopback:8080) + `mysql:8`(volume) + `alloy`(profile `monitoring`, mem 160m). nginx 443→8080(WS upgrade 포함).
- **프론트 VM**: nginx 443 → Next.js standalone 컨테이너.
- **상태**: OCI Object Storage 버킷(S3 호환 backend).
- **시크릿**: VM의 `/opt/knup/.env`(DB·Gemini·Grafana Cloud), GitHub Actions Secrets(OCI·SSH).

## 9. 모니터링 (Grafana Cloud Free + Alloy)

```
backend:8081/actuator/prometheus ──scrape(30s)──▶ Alloy(백엔드 VM) ──remote_write──▶ Grafana Cloud
```
- 시계열 저장·대시보드·알림은 **Grafana Cloud(SaaS)** — 1GB VM 부담 회피.
- 대시보드: 마켓 `11378`(Spring Boot System) + 레포 `infra/grafana/knup-backend-overview.json`(요청률/p95·p99/5xx/힙/GC/스레드/CPU/HikariCP).
- 알림(예): 5xx>5%, 힙>90%, p95>0.5s.

## 10. CI/CD

| 레포 | 트리거 | 동작 |
|---|---|---|
| frontend / backend | PR | 빌드(+lint/compile) |
| frontend / backend | `main` push | ghcr 이미지 빌드·푸시 → **SSH로 VM에 `docker load` + `compose up`** (자동배포) |
| infra | PR / `main` push | OpenTofu **plan / apply** |
- 커밋 컨벤션: Conventional Commits, PR 기반 머지.

## 11. 보안 · 알려진 제약 (실측)

- **cross-domain 세션 쿠키**: 프론트(158) ↔ 백엔드(144) 도메인 분리 → 일부 브라우저의 third-party 쿠키 차단 시 세션 유지 이슈 가능. (`/login`에서 로그인 후 풀리면 이 원인)
- **HttpSession 인메모리**: 백엔드 재배포 시 로그인 세션 소멸(영속화 미적용).
- **WebSocket 무인증**: 참가자 무로그인 설계상 채널 보호 없음.
- **1 GB VM**: backend(JVM)+MySQL+Alloy가 swap 의존. 메모리 여유 작음.

## 12. 화면 흐름 요약

- **참가자**: 랜딩 → `/join`(PIN+닉네임) → `/play/{id}/waiting`(라운지) → `question`(타이머 링·4색 도형) → `result`(정답 셀러브레이션/오답 위로·카운트업) → `leaderboard`(골드 1등·내 팀 강조).
- **호스트**: `/login` → 대시보드(내 퀴즈 서재 / AI 생성) → 퀴즈 작성(아코디언 폼) → `/host/sessions/new` → `waiting`(PIN·실시간 인원·select 강퇴·세션 종료) → `play`(PD 조종석: 문제·정답률·실시간 TOP5) → `result`(통계·골드 시상).

## 13. 제안서 대비 실제 차이 (중요)

| 항목 | 제안서 | **실제 구현** |
|---|---|---|
| 인증 | Spring Security + **JWT** | 커스텀 `AuthInterceptor` + **HttpSession 쿠키** |
| 프론트 배포 | **Vercel** | **OCI VM**(자체 nginx + Next standalone) |
| VM 스펙 | **ARM A1 4 OCPU / 24 GB** | **AMD E2.1.Micro 1 OCPU / 1 GB × 2대** (A1 capacity 부재) |
| 모니터링 | self-host **Prometheus + Grafana** | **Grafana Cloud Free + Alloy**(VM 메모리 제약) |
| IaC | Terraform | **OpenTofu** |
| 답안 전송 | WebSocket 발행 | **REST `POST /answer`** (WS는 수신·heartbeat) |
| Frontend 버전 | Next 14 / React 18 | **Next 16 / React 19** |
| DB | MySQL 8 (Docker) | 동일 ✅ |
| AI | Gemini | 동일 ✅ (`gemini-2.5-flash-lite`) |
| 팀전·리더보드·CI/CD·Docker | 계획 | 구현됨 ✅ |
