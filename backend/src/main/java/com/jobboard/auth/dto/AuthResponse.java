package com.jobboard.auth.dto;

import com.jobboard.user.UserDto;

import java.time.Instant;

public record AuthResponse(
        String token,
        Instant expiresAt,
        UserDto user) {
}
