package com.knupbackend.auth.service;

import com.knupbackend.auth.dto.request.LoginRequest;
import com.knupbackend.auth.dto.request.SignUpRequest;
import com.knupbackend.auth.dto.response.AuthResponse;
import com.knupbackend.global.auth.LoginSessionConst;
import com.knupbackend.global.exception.ErrorCode;
import com.knupbackend.global.exception.KnupException;
import com.knupbackend.user.domain.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class AuthServiceTest {

    @Autowired AuthService authService;
    @Autowired UserRepository userRepository;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        request = new MockHttpServletRequest();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 - 이메일 중복 시 EMAIL_DUPLICATE 예외 발생")
    void signUp_duplicateEmail_throwsException() {
        authService.signUp(signUpRequest("test@test.com"), request);

        assertThatThrownBy(() ->
                authService.signUp(signUpRequest("test@test.com"), new MockHttpServletRequest()))
                .isInstanceOf(KnupException.class)
                .satisfies(e ->
                        assertThat(((KnupException) e).getErrorCode())
                                .isEqualTo(ErrorCode.EMAIL_DUPLICATE));
    }

    @Test
    @DisplayName("회원가입 - 성공 시 DB에 사용자 저장")
    void signUp_success_savesUser() {
        AuthResponse result = authService.signUp(signUpRequest("test@test.com"), request);

        assertThat(result.getEmail()).isEqualTo("test@test.com");
        assertThat(result.getNickname()).isEqualTo("테스터");
        assertThat(userRepository.findByEmail("test@test.com")).isPresent();
    }

    @Test
    @DisplayName("회원가입 - 성공 시 세션에 userId 저장")
    void signUp_success_savesUserIdToSession() {
        authService.signUp(signUpRequest("test@test.com"), request);

        assertThat(request.getSession(false)).isNotNull();
        assertThat(request.getSession(false).getAttribute(LoginSessionConst.LOGIN_USER_ID)).isNotNull();
    }

    @Test
    @DisplayName("로그인 - 존재하지 않는 이메일 시 AUTHENTICATION_FAILED 예외 발생")
    void login_emailNotFound_throwsException() {
        assertThatThrownBy(() ->
                authService.login(loginRequest("none@test.com", "password123"), request))
                .isInstanceOf(KnupException.class)
                .satisfies(e ->
                        assertThat(((KnupException) e).getErrorCode())
                                .isEqualTo(ErrorCode.AUTHENTICATION_FAILED));
    }

    @Test
    @DisplayName("로그인 - 비밀번호 불일치 시 AUTHENTICATION_FAILED 예외 발생")
    void login_wrongPassword_throwsException() {
        authService.signUp(signUpRequest("test@test.com"), request);

        assertThatThrownBy(() ->
                authService.login(loginRequest("test@test.com", "wrongpass"), new MockHttpServletRequest()))
                .isInstanceOf(KnupException.class)
                .satisfies(e ->
                        assertThat(((KnupException) e).getErrorCode())
                                .isEqualTo(ErrorCode.AUTHENTICATION_FAILED));
    }

    @Test
    @DisplayName("로그인 - 성공 시 세션에 userId 저장")
    void login_success_savesUserIdToSession() {
        authService.signUp(signUpRequest("test@test.com"), request);

        MockHttpServletRequest loginReq = new MockHttpServletRequest();
        AuthResponse result = authService.login(loginRequest("test@test.com", "password123"), loginReq);

        assertThat(result.getEmail()).isEqualTo("test@test.com");
        assertThat(loginReq.getSession(false).getAttribute(LoginSessionConst.LOGIN_USER_ID)).isNotNull();
    }

    @Test
    @DisplayName("로그아웃 - 세션 무효화")
    void logout_invalidatesSession() {
        authService.signUp(signUpRequest("test@test.com"), request);

        authService.logout(request);

        assertThat(request.getSession(false)).isNull();
    }

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
}
