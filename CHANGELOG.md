# Changelog

이 프로젝트의 주요 변경 사항을 기록합니다. 형식은 [Keep a Changelog](https://keepachangelog.com/ko/1.1.0/),
버전은 [Semantic Versioning](https://semver.org/lang/ko/)을 따릅니다.

## [Unreleased]
### 계획
- HttpSession 영속화(Redis), WebSocket 인증 토큰, third-party 쿠키 대응 — [로드맵](https://github.com/knup-project/.github/blob/main/docs/ROADMAP.md)

## [1.0.0] - 2026-06-12
### Added
- Actuator + Micrometer Prometheus 메트릭(관리 포트 `8081` 분리)
- 호스트 참가자 강퇴 API(단건/일괄/전체), 유령 세션 자동 종료 스케줄러
### Changed
- `SessionResponse` 계약 정합성 정리 (`sessionId` + `participants`, 팀 `teamId=teamName`)
- 세션 조회(`GET /sessions/{id}`)를 인증 제외로 공개, CORS preflight 인증 제외
### Fixed
- 게임 진행·AI 생성 안정화(복구 필드, 팀 자동배정, Gemini 타임아웃)
- 크로스사이트 세션 쿠키(`SameSite=None; Secure`) + forwarded-headers

## [0.3.0] - 2026-06-04
### Added
- AI 퀴즈 생성(텍스트/PDF) 및 해설 — Google Gemini 연동

## [0.2.0] - 2026-05-31
### Added
- 실시간 세션 코어: WebSocket(STOMP) 게임 진행, 리더보드, 참가 플로우

## [0.1.0] - 2026-05-27
### Added
- 도메인 모델(User·Quiz·Question·GameSession·Participant·Answer)
- 커스텀 인증(AuthInterceptor + HttpSession), 퀴즈 CRUD

[Unreleased]: https://github.com/knup-project/backend/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/knup-project/backend/releases/tag/v1.0.0
[0.3.0]: https://github.com/knup-project/backend/releases/tag/v0.3.0
[0.2.0]: https://github.com/knup-project/backend/releases/tag/v0.2.0
[0.1.0]: https://github.com/knup-project/backend/releases/tag/v0.1.0
