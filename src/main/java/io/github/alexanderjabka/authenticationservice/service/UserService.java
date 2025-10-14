package io.github.alexanderjabka.authenticationservice.service;

import io.github.alexanderjabka.authenticationservice.dto.RegisterRequest;
import io.github.alexanderjabka.authenticationservice.entity.User;
import io.github.alexanderjabka.authenticationservice.exception.ConflictException;
import io.github.alexanderjabka.authenticationservice.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail));
    }

    @Transactional
    public User register(RegisterRequest request) {
        userRepository.findByUsername(request.username()).ifPresent(u -> {
            throw new ConflictException("Username is already taken");
        });
        userRepository.findByEmail(request.email()).ifPresent(u -> {
            throw new ConflictException("Email is already registered");
        });
        String salt = generateSalt();
        String hashed = hashPassword(request.password(), salt);

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setSalt(salt);
        user.setPassword(hashed);
        return userRepository.save(user);
    }

    public boolean checkPassword(User user, String rawPassword) {
        String combined = rawPassword + ":" + user.getSalt();
        return BCrypt.checkpw(combined, user.getPassword());
    }

    private String generateSalt() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashPassword(String rawPassword, String salt) {
        String combined = rawPassword + ":" + salt;
        String bcryptSalt = BCrypt.gensalt(12);
        return BCrypt.hashpw(combined, bcryptSalt);
    }
}


