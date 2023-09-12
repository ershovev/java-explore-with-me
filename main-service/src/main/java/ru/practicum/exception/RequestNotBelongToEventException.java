package ru.practicum.exception;

public class RequestNotBelongToEventException extends RuntimeException {
    public RequestNotBelongToEventException(String message) {
        super(message);
    }
}
