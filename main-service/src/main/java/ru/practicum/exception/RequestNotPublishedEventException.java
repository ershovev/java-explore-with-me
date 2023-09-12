package ru.practicum.exception;

public class RequestNotPublishedEventException extends RuntimeException {
    public RequestNotPublishedEventException(String message) {
        super(message);
    }
}
