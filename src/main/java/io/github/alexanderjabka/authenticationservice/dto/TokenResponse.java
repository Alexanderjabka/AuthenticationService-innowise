package io.github.alexanderjabka.authenticationservice.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        long accessExpiresInSeconds,
        long refreshExpiresInSeconds
) {}


