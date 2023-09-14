package ru.practicum.exception;

public class EventFullParticipantLimit extends RuntimeException {
    public EventFullParticipantLimit(String message) {
        super(message);
    }
}