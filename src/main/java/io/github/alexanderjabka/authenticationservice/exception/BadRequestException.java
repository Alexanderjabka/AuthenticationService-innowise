package io.github.alexanderjabka.authenticationservice.exception;

public class BadRequestException extends ApiException {
    public BadRequestException(String message) {
        super("BAD_REQUEST", message);
    }
}


