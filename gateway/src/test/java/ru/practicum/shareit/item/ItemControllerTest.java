package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@DisplayName("Тесты REST-контроллера вещей в шлюзе")
class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    @DisplayName("Передаёт получение вещи по идентификатору в клиент")
    void getByIdShouldDelegateToClient() throws Exception {
        when(itemClient.getById(1L, 2L)).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(get("/items/1").header("X-Sharer-User-Id", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Передаёт получение вещей пользователя в клиент")
    void getAllItemsFromUserShouldDelegateToClient() throws Exception {
        when(itemClient.getAllItemsFromUser(1L)).thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1))));

        mockMvc.perform(get("/items").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("Передаёт параметры поиска вещей в клиент")
    void searchShouldDelegateQueryParamsToClient() throws Exception {
        when(itemClient.getAllAvailableByNameAndDescription(eq(1L), any()))
                .thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/items/search").param("text", "дрель").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Валидирует и передаёт создание вещи в клиент")
    void addNewItemShouldValidateAndDelegateToClient() throws Exception {
        when(itemClient.addNewItem(eq(1L), any())).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Отклоняет создание вещи без названия")
    void addNewItemShouldRejectMissingName() throws Exception {
        ItemDto dto = itemDto();
        dto.setName(null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Валидирует и передаёт создание комментария в клиент")
    void commentShouldValidateAndDelegateToClient() throws Exception {
        when(itemClient.commentItem(any(), eq(2L), eq(1L))).thenReturn(ResponseEntity.status(201).body(Map.of("id", 1)));
        CommentRequestDto dto = new CommentRequestDto();
        dto.setText("Текст комментария");

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Передаёт обновление и удаление вещи в клиент")
    void updateAndDeleteShouldDelegateToClient() throws Exception {
        when(itemClient.updateItem(eq(1L), any(), eq(2L))).thenReturn(ResponseEntity.ok(Map.of("id", 2)));
        when(itemClient.deleteItem(1L, 2L)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/items/2")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto())))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/items/2").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());

        verify(itemClient).deleteItem(1L, 2L);
    }

    private static ItemDto itemDto() {
        return ItemDto.builder()
                .name("Вещь")
                .description("Описание вещи")
                .available(true)
                .build();
    }
}
