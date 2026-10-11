package ru.practicum.shareit.exception;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {
    private final String rejectedValue;
    private final String field;

    public ValidationException(String message, Object rejectedValue, String field) {
        super(message);
        this.rejectedValue = rejectedValue.toString();
        this.field = field;
    }
}
