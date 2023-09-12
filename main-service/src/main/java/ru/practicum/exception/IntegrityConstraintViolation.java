package ru.practicum.exception;

public class IntegrityConstraintViolation extends RuntimeException {
    public IntegrityConstraintViolation(String message) {
        super(message);
    }
}
