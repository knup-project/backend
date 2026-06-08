package com.knupbackend.session.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 방치된(유령) 세션 자동 정리 스케줄러.
 * 생성 후 일정 시간이 지난 미종료 세션을 주기적으로 종료한다.
 */
@Component
@RequiredArgsConstructor
public class SessionCleanupScheduler {

    private final SessionService sessionService;

    /** 10분마다 실행 */
    @Scheduled(fixedDelay = 600_000L)
    public void expireStaleSessions() {
        sessionService.expireStaleSessions();
    }
}
