package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.entity.User;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@DisplayName("Тесты REST-контроллера пользователей на сервере")
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("Возвращает пользователя по идентификатору")
    void getUserShouldReturnUser() throws Exception {
        when(userService.getUser(1L)).thenReturn(user(1L));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user1@example.com"));
    }

    @Test
    @DisplayName("Создаёт пользователя и возвращает статус 201")
    void createUserShouldReturnCreatedUser() throws Exception {
        when(userService.saveUser(any())).thenReturn(user(1L));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Пользователь"));
    }

    @Test
    @DisplayName("Обновляет пользователя")
    void updateUserShouldReturnUpdatedUser() throws Exception {
        when(userService.updateUser(any(), eq(1L))).thenReturn(user(1L));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Удаляет пользователя и возвращает статус 204")
    void deleteUserShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    private static User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("user" + id + "@example.com");
        user.setName("Пользователь");
        return user;
    }

    private static UserDto userDto() {
        UserDto dto = new UserDto();
        dto.setEmail("user1@example.com");
        dto.setName("Пользователь");
        return dto;
    }
}
