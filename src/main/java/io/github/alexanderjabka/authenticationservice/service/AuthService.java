package io.github.alexanderjabka.authenticationservice.service;

import io.github.alexanderjabka.authenticationservice.dto.LoginRequest;
import io.github.alexanderjabka.authenticationservice.dto.TokenResponse;
import io.github.alexanderjabka.authenticationservice.dto.ValidateTokenResponse;
import io.github.alexanderjabka.authenticationservice.entity.User;
import io.github.alexanderjabka.authenticationservice.exception.UnauthorizedException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class AuthService {
    private final UserService userService;

    private final SecretKey secretKey;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;

    public AuthService(
            UserService userService,
            @Value("${jwt.secret:change-me-change-me-change-me-change-me}") String secret,
            @Value("${jwt.access-token-ttl:900}") long accessTtlSeconds,
            @Value("${jwt.refresh-token-ttl:604800}") long refreshTtlSeconds
    ) {
        this.userService = userService;
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
    }

    public TokenResponse login(LoginRequest request) {
        Optional<User> userOpt = userService.findByUsernameOrEmail(request.usernameOrEmail());
        if (userOpt.isEmpty()) {
            throw new UnauthorizedException("Invalid credentials");
        }
        User user = userOpt.get();
        if (!userService.checkPassword(user, request.password())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        String access = generateToken(user, accessTtlSeconds, "ACCESS");
        String refresh = generateToken(user, refreshTtlSeconds, "REFRESH");
        return new TokenResponse(access, refresh, accessTtlSeconds, refreshTtlSeconds);
    }

    public ValidateTokenResponse validate(String token) {
        try {
            var claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Long userId = claims.get("userId", Number.class).longValue();
            String username = claims.getSubject();
            return new ValidateTokenResponse(true, userId, username);
        } catch (Exception e) {
            return new ValidateTokenResponse(false, null, null);
        }
    }

    public TokenResponse refresh(String refreshToken) {
        var result = validate(refreshToken);
        if (!result.valid()) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        Optional<User> userOpt = userService.findByUsernameOrEmail(result.username());
        if (userOpt.isEmpty()) {
            throw new UnauthorizedException("User not found");
        }
        User user = userOpt.get();
        String access = generateToken(user, accessTtlSeconds, "ACCESS");
        String refresh = generateToken(user, refreshTtlSeconds, "REFRESH");
        return new TokenResponse(access, refresh, accessTtlSeconds, refreshTtlSeconds);
    }

    private String generateToken(User user, long ttlSeconds, String type) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .claim("type", type)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
}


