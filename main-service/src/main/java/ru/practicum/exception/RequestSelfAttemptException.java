package ru.practicum.exception;

public class RequestSelfAttemptException extends RuntimeException {
    public RequestSelfAttemptException(String message) {
        super(message);
    }
}
