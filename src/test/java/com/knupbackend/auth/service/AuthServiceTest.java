package com.knupbackend.auth.service;

import com.knupbackend.auth.dto.request.LoginRequest;
import com.knupbackend.auth.dto.request.SignUpRequest;
import com.knupbackend.auth.dto.response.AuthResponse;
import com.knupbackend.global.auth.LoginSessionConst;
import com.knupbackend.global.exception.ErrorCode;
import com.knupbackend.global.exception.KnupException;
import com.knupbackend.user.entity.User;
import com.knupbackend.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AuthService authService;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
    }

    // ══════════════════════════════════════════════════════════════
    // 회원가입
    // ══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("회원가입 - 이메일 중복 시 EMAIL_DUPLICATE 예외 발생")
    void signUp_duplicateEmail_throwsException() {
        given(userRepository.existsByEmail("test@test.com")).willReturn(true);

        assertThatThrownBy(() -> authService.signUp(signUpRequest("test@test.com"), request))
                .isInstanceOf(KnupException.class)
                .satisfies(e ->
                        assertThat(((KnupException) e).getErrorCode())
                                .isEqualTo(ErrorCode.EMAIL_DUPLICATE));
    }

    @Test
    @DisplayName("회원가입 - 성공 시 DB에 사용자 저장")
    void signUp_success_savesUser() {
        given(userRepository.existsByEmail(any())).willReturn(false);
        given(passwordEncoder.encode(any())).willReturn("encoded");
        given(userRepository.save(any())).willReturn(savedUser());

        AuthResponse result = authService.signUp(signUpRequest("test@test.com"), request);

        assertThat(result.getEmail()).isEqualTo("test@test.com");
        assertThat(result.getNickname()).isEqualTo("테스터");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 - 성공 시 세션에 userId 저장")
    void signUp_success_savesUserIdToSession() {
        given(userRepository.existsByEmail(any())).willReturn(false);
        given(passwordEncoder.encode(any())).willReturn("encoded");
        given(userRepository.save(any())).willReturn(savedUser());

        authService.signUp(signUpRequest("test@test.com"), request);

        HttpSession session = request.getSession(false);
        assertThat(session).isNotNull();
        assertThat(session.getAttribute(LoginSessionConst.LOGIN_USER_ID)).isEqualTo(1L);
    }

    // ══════════════════════════════════════════════════════════════
    // 로그인
    // ══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("로그인 - 존재하지 않는 이메일 시 AUTHENTICATION_FAILED 예외 발생")
    void login_emailNotFound_throwsException() {
        given(userRepository.findByEmail(any())).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest("none@test.com", "pw"), request))
                .isInstanceOf(KnupException.class)
                .satisfies(e ->
                        assertThat(((KnupException) e).getErrorCode())
                                .isEqualTo(ErrorCode.AUTHENTICATION_FAILED));
    }

    @Test
    @DisplayName("로그인 - 비밀번호 불일치 시 AUTHENTICATION_FAILED 예외 발생")
    void login_wrongPassword_throwsException() {
        given(userRepository.findByEmail(any())).willReturn(Optional.of(savedUser()));
        given(passwordEncoder.matches(any(), any())).willReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest("test@test.com", "wrong"), request))
                .isInstanceOf(KnupException.class)
                .satisfies(e ->
                        assertThat(((KnupException) e).getErrorCode())
                                .isEqualTo(ErrorCode.AUTHENTICATION_FAILED));
    }

    @Test
    @DisplayName("로그인 - 성공 시 세션에 userId 저장")
    void login_success_savesUserIdToSession() {
        given(userRepository.findByEmail(any())).willReturn(Optional.of(savedUser()));
        given(passwordEncoder.matches(any(), any())).willReturn(true);

        AuthResponse result = authService.login(loginRequest("test@test.com", "password123"), request);

        assertThat(result.getEmail()).isEqualTo("test@test.com");
        HttpSession session = request.getSession(false);
        assertThat(session).isNotNull();
        assertThat(session.getAttribute(LoginSessionConst.LOGIN_USER_ID)).isEqualTo(1L);
    }

    // ══════════════════════════════════════════════════════════════
    // 로그아웃
    // ══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("로그아웃 - 세션 무효화")
    void logout_invalidatesSession() {
        request.getSession(true).setAttribute(LoginSessionConst.LOGIN_USER_ID, 1L);

        authService.logout(request);

        assertThat(request.getSession(false)).isNull();
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────

    private SignUpRequest signUpRequest(String email) {
        SignUpRequest req = new SignUpRequest();
        req.setEmail(email);
        req.setPassword("password123");
        req.setNickname("테스터");
        return req;
    }

    private LoginRequest loginRequest(String email, String password) {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    private User savedUser() {
        return User.builder()
                .id(1L)
                .email("test@test.com")
                .password("encoded")
                .nickname("테스터")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
