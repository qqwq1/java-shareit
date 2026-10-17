package ru.practicum.shareit.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.entity.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Интеграционные тесты сервиса пользователей")
class UserServiceImplIntegrationTest {
    @Autowired
    private UserService userService;

    @Test
    @DisplayName("Сохраняет пользователя и отклоняет дублирующийся адрес электронной почты")
    void saveUserShouldPersistAndRejectDuplicateEmail() {
        UserDto userDto = userDto("user@example.com", "Пользователь");

        User saved = userService.saveUser(userDto);

        assertThat(saved.getId()).isNotNull();
        assertThat(userService.getUser(saved.getId()).getEmail()).isEqualTo("user@example.com");
        assertThatThrownBy(() -> userService.saveUser(userDto("USER@example.com", "Другой пользователь")))
                .isInstanceOf(DuplicateException.class);
    }

    @Test
    @DisplayName("Обновляет только переданные поля пользователя")
    void updateUserShouldPatchOnlyProvidedFields() {
        User saved = userService.saveUser(userDto("old@example.com", "Старое имя"));
        UserDto patch = new UserDto();
        patch.setName("Новое имя");

        User updated = userService.updateUser(patch, saved.getId());

        assertThat(updated.getName()).isEqualTo("Новое имя");
        assertThat(updated.getEmail()).isEqualTo("old@example.com");
    }

    @Test
    @DisplayName("Удаляет пользователя из базы данных")
    void deleteUserShouldRemoveEntity() {
        User saved = userService.saveUser(userDto("delete@example.com", "Пользователь для удаления"));

        userService.deleteUser(saved.getId());

        assertThat(userService.isUserExists(saved.getId())).isFalse();
        assertThatThrownBy(() -> userService.getUser(saved.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    private static UserDto userDto(String email, String name) {
        UserDto dto = new UserDto();
        dto.setEmail(email);
        dto.setName(name);
        return dto;
    }
}
