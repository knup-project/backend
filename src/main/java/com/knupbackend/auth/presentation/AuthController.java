package com.knupbackend.auth.presentation;

import com.knupbackend.auth.dto.request.LoginRequest;
import com.knupbackend.auth.dto.request.SignUpRequest;
import com.knupbackend.auth.dto.response.AuthResponse;
import com.knupbackend.auth.service.AuthService;
import com.knupbackend.global.auth.LoginUser;
import com.knupbackend.user.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** POST /api/v1/auth/signup — 회원가입 */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody SignUpRequest request,
                                               HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.signUp(request, httpRequest));
    }

    /** POST /api/v1/auth/login — 로그인 */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.login(request, httpRequest));
    }

    /** POST /api/v1/auth/logout — 로그아웃 */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {
        authService.logout(httpRequest);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/v1/auth/me — 현재 로그인 사용자 (세션 검증용). 미로그인 시 인터셉터가 401 처리. */
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(@LoginUser User user) {
        return ResponseEntity.ok(AuthResponse.from(user));
    }
}
