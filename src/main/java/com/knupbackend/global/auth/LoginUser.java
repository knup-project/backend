package com.knupbackend.global.auth;

import java.lang.annotation.*;

/**
 * 컨트롤러 파라미터에 붙이면 세션에서 로그인 유저를 주입해 줍니다.
 * Spring Security 없이 직접 구현한 인증 어노테이션입니다.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoginUser {}
