package io.github.alexanderjabka.authenticationservice.dto;

import jakarta.validation.constraints.NotBlank;

public record ValidateTokenRequest(
        @NotBlank String token
) {}


