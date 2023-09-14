package ru.practicum.exception;

public class EventModerationException extends RuntimeException {
    public EventModerationException(String message) {
        super(message);
    }
}