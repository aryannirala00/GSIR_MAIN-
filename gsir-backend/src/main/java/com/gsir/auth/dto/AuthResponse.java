package com.gsir.auth.dto;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    long expiresIn
) {}
