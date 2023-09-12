package ru.practicum.exception;

public class RequestFullOccupiedException extends RuntimeException {
    public RequestFullOccupiedException(String message) {
        super(message);
    }
}
