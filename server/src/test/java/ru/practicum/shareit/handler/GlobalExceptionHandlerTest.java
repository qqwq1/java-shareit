package ru.practicum.shareit.handler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тесты обработчика исключений")
class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Возвращает описание ошибки для отсутствующей сущности")
    void handleNotFoundExceptionShouldReturnErrorResponse() {
        List<ErrorResponse> response = handler.handleNotFoundException(
                new NotFoundException("Пользователь не найден", "1", "id"));

        assertThat(response).singleElement()
                .satisfies(error -> {
                    assertThat(error.getField()).isEqualTo("id");
                    assertThat(error.getRejectedValue()).isEqualTo("1");
                    assertThat(error.getDescription()).isEqualTo("Пользователь не найден");
                });
    }

    @Test
    @DisplayName("Возвращает описание ошибки валидации")
    void handleValidationExceptionShouldReturnErrorResponse() {
        List<ErrorResponse> response = handler.handleValidationException(
                new ValidationException("Некорректное значение", "пусто", "name"));

        assertThat(response).singleElement()
                .satisfies(error -> {
                    assertThat(error.getField()).isEqualTo("name");
                    assertThat(error.getRejectedValue()).isEqualTo("пусто");
                    assertThat(error.getDescription()).isEqualTo("Некорректное значение");
                });
    }

    @Test
    @DisplayName("Возвращает описание ошибки дублирования")
    void handleDuplicateExceptionShouldReturnErrorResponse() {
        List<ErrorResponse> response = handler.handleDuplicateException(
                new DuplicateException("Адрес уже занят", "user@example.com", "email"));

        assertThat(response).singleElement()
                .satisfies(error -> {
                    assertThat(error.getField()).isEqualTo("email");
                    assertThat(error.getRejectedValue()).isEqualTo("user@example.com");
                    assertThat(error.getDescription()).isEqualTo("Адрес уже занят");
                });
    }

    @Test
    @DisplayName("Возвращает описание непредвиденной ошибки")
    void handleGeneralExceptionShouldReturnErrorResponse() {
        List<ErrorResponse> response = handler.handleGeneralException(new RuntimeException("Сбой"));

        assertThat(response).singleElement()
                .satisfies(error -> {
                    assertThat(error.getField()).isEmpty();
                    assertThat(error.getRejectedValue()).isEmpty();
                    assertThat(error.getDescription()).isEqualTo("Сбой");
                });
    }
}
