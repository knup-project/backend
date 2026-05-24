package com.knupbackend.auth.dto.response;

import com.knupbackend.user.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuthResponse {

    private Long id;
    private String email;
    private String nickname;
    private LocalDateTime createdAt;

    public static AuthResponse from(User user) {
        return AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
