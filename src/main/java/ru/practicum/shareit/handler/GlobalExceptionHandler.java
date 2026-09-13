package ru.practicum.shareit.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public List<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<ErrorResponse> errorResponseList = new ArrayList<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(fieldError -> {
                    ErrorResponse errorResponse = new ErrorResponse();
                    errorResponse.setField(fieldError.getField());
                    String rejectedValue = fieldError.getRejectedValue() == null ?
                            "null" : fieldError.getRejectedValue().toString();
                    errorResponse.setRejectedValue(rejectedValue);
                    errorResponse.setDescription(fieldError.getDefaultMessage());
                    errorResponseList.add(errorResponse);

                    log.debug("Ошибка валидации для поля {}, отклоненное значение \"{}\"\n сообщение: {}",
                            errorResponse.getField(),
                            errorResponse.getRejectedValue(),
                            errorResponse.getDescription());
                });
        log.error("Ошибка валидации: {}", errorResponseList);
        return errorResponseList;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public List<ErrorResponse> handleNotFoundException(NotFoundException ex) {
        log.error("Ошибка при поиске сущности: {}", ex.getMessage());
        return List.of(new ErrorResponse(ex.getField(), ex.getRejectedValue(), ex.getMessage()));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ExceptionHandler(ValidationException.class)
    public List<ErrorResponse> handleValidationException(ValidationException ex) {
        log.error("Ошибка при поиске по id: {}", ex.getMessage());
        return List.of(new ErrorResponse(ex.getField(), ex.getRejectedValue(), ex.getMessage()));
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicateException.class)
    public List<ErrorResponse> handleDuplicateException(DuplicateException ex) {
        log.error("Попытка записи дублирующих данных: {}", ex.getMessage());
        return List.of(new ErrorResponse(ex.getField(), ex.getRejectedValue(), ex.getMessage()));
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public List<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Unexpected error", ex);
        return List.of(new ErrorResponse("", "", ex.getMessage()));
    }
}
