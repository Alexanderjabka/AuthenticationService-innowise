package io.github.alexanderjabka.authenticationservice.exception;

public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super("CONFLICT", message);
    }
}


