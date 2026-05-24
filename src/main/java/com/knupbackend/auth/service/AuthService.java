package com.knupbackend.auth.service;

import com.knupbackend.auth.dto.request.LoginRequest;
import com.knupbackend.auth.dto.request.SignUpRequest;
import com.knupbackend.auth.dto.response.AuthResponse;
import com.knupbackend.global.auth.LoginSessionConst;
import com.knupbackend.global.exception.ErrorCode;
import com.knupbackend.global.exception.KnupException;
import com.knupbackend.user.entity.User;
import com.knupbackend.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** 회원가입 + 자동 로그인 */
    @Transactional
    public AuthResponse signUp(SignUpRequest request, HttpServletRequest httpRequest) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new KnupException(ErrorCode.EMAIL_DUPLICATE);
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .build();

        User saved = userRepository.save(user);
        saveSession(saved.getId(), httpRequest);
        return AuthResponse.from(saved);
    }

    /** 로그인 → 세션에 userId 저장 */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new KnupException(ErrorCode.AUTHENTICATION_FAILED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new KnupException(ErrorCode.AUTHENTICATION_FAILED);
        }

        saveSession(user.getId(), httpRequest);
        return AuthResponse.from(user);
    }

    /** 로그아웃 → 세션 무효화 */
    public void logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    // ── 세션 저장 ──────────────────────────────────────────────────

    private void saveSession(Long userId, HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(LoginSessionConst.LOGIN_USER_ID, userId);
    }
}
