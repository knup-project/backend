package com.knupbackend.auth.dto.response;

import com.knupbackend.user.domain.User;

import java.time.LocalDateTime;

public record AuthResponse(
        Long id,
        String email,
        String nickname,
        LocalDateTime createdAt
) {
    public static AuthResponse from(User user) {
        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getCreatedAt()
        );
    }
}
