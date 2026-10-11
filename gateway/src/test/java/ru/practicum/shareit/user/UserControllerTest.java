package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Map;

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
@DisplayName("Тесты REST-контроллера пользователей в шлюзе")
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    @Test
    @DisplayName("Передаёт получение пользователя в клиент")
    void getUserShouldDelegateToClient() throws Exception {
        when(userClient.getUser(1L)).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Валидирует и передаёт создание пользователя в клиент")
    void createUserShouldValidateAndDelegateToClient() throws Exception {
        when(userClient.createUser(any())).thenReturn(ResponseEntity.status(201).body(Map.of("id", 1)));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto("user@example.com", "Пользователь"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Отклоняет создание пользователя с некорректным адресом электронной почты")
    void createUserShouldRejectInvalidEmail() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto("bad-email", "Пользователь"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Передаёт обновление пользователя в клиент")
    void updateUserShouldDelegateToClient() throws Exception {
        when(userClient.updateUser(eq(1L), any())).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto("new@example.com", null))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Передаёт удаление пользователя в клиент")
    void deleteUserShouldDelegateToClient() throws Exception {
        when(userClient.deleteUser(1L)).thenReturn(ResponseEntity.noContent().build());

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(userClient).deleteUser(1L);
    }

    private static UserDto userDto(String email, String name) {
        UserDto dto = new UserDto();
        dto.setEmail(email);
        dto.setName(name);
        return dto;
    }
}
