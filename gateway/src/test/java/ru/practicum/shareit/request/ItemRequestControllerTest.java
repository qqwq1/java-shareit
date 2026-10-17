package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
@DisplayName("Тесты REST-контроллера запросов вещей в шлюзе")
class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    @Test
    @DisplayName("Валидирует и передаёт создание запроса в клиент")
    void addNewRequestShouldValidateAndDelegateToClient() throws Exception {
        when(itemRequestClient.addNewRequest(any(), eq(1L))).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto("Нужна вещь"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Отклоняет создание запроса с пустым описанием")
    void addNewRequestShouldRejectBlankDescription() throws Exception {
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto(""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Передаёт запросы на чтение в клиент")
    void getEndpointsShouldDelegateToClient() throws Exception {
        when(itemRequestClient.getAllRequestsByUser(1L)).thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1))));
        when(itemRequestClient.getAllRequests(1L)).thenReturn(ResponseEntity.ok(List.of(Map.of("id", 2))));
        when(itemRequestClient.getRequestById(2L, 1L)).thenReturn(ResponseEntity.ok(Map.of("id", 2)));

        mockMvc.perform(get("/requests").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
        mockMvc.perform(get("/requests/all").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
        mockMvc.perform(get("/requests/2").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    private static CreateItemRequestDto requestDto(String description) {
        CreateItemRequestDto dto = new CreateItemRequestDto();
        dto.setDescription(description);
        return dto;
    }
}
