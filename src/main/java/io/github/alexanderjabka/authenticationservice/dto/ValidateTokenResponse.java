package io.github.alexanderjabka.authenticationservice.dto;

public record ValidateTokenResponse(
        boolean valid,
        Long userId,
        String username
) {}


