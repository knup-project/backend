package com.knupbackend.global.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knupbackend.global.exception.ErrorCode;
import com.knupbackend.global.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // CORS preflight(OPTIONS) 요청은 인증 없이 통과시킨다.
        // 막으면 preflight 가 401 이 되어 브라우저가 본요청을 CORS 로 차단한다.
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute(LoginSessionConst.LOGIN_USER_ID) == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(
                    objectMapper.writeValueAsString(ErrorResponse.of(ErrorCode.UNAUTHORIZED))
            );
            return false;
        }

        return true;
    }
}
