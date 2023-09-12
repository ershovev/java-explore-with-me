package ru.practicum.exception;

public class RequestSecondAttemptException extends RuntimeException {
    public RequestSecondAttemptException(String message) {
        super(message);
    }
}
