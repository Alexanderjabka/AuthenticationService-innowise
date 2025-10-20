package io.github.alexanderjabka.authenticationservice.controller;

import io.github.alexanderjabka.authenticationservice.dto.LoginRequest;
import io.github.alexanderjabka.authenticationservice.dto.RegisterRequest;
import io.github.alexanderjabka.authenticationservice.dto.TokenResponse;
import io.github.alexanderjabka.authenticationservice.dto.ValidateTokenRequest;
import io.github.alexanderjabka.authenticationservice.dto.ValidateTokenResponse;
import io.github.alexanderjabka.authenticationservice.service.AuthService;
import io.github.alexanderjabka.authenticationservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequest request) {
        userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/validate")
    public ValidateTokenResponse validate(@RequestBody @Valid ValidateTokenRequest request) {
        return authService.validate(request.token());
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody @Valid ValidateTokenRequest request) {
        return authService.refresh(request.token());
    }
}
