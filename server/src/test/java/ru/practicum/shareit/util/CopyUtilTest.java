package ru.practicum.shareit.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Тесты утилиты копирования свойств")
class CopyUtilTest {

    @Test
    @DisplayName("Копирует только непустые поля")
    void copyNonNullPropertiesShouldCopyOnlyFilledFields() {
        UserDto source = new UserDto();
        source.setName("Новое имя");
        UserDto target = new UserDto();
        target.setEmail("user@example.com");
        target.setName("Старое имя");

        CopyUtil.copyNonNullProperties(source, target);

        assertThat(target.getName()).isEqualTo("Новое имя");
        assertThat(target.getEmail()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("Отклоняет пустой источник и пустую цель")
    void copyNonNullPropertiesShouldRejectNullArguments() {
        UserDto target = new UserDto();

        assertThatThrownBy(() -> CopyUtil.copyNonNullProperties(null, target))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> CopyUtil.copyNonNullProperties(new UserDto(), null))
                .isInstanceOf(ValidationException.class);
    }
}
