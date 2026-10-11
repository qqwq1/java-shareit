package ru.practicum.shareit.exception;

import lombok.Getter;

@Getter
public class DuplicateException extends RuntimeException {
    private final String rejectedValue;
    private final String field;

    public DuplicateException(String message, String rejectedValue, String field) {
        super(message);
        this.rejectedValue = rejectedValue;
        this.field = field;
    }
}
