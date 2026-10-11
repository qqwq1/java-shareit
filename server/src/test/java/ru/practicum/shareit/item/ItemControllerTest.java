package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.entity.Comment;
import ru.practicum.shareit.item.entity.Item;
import ru.practicum.shareit.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

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

@WebMvcTest(ItemController.class)
@DisplayName("Тесты REST-контроллера вещей на сервере")
class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    @DisplayName("Возвращает вещь по идентификатору")
    void getShouldReturnItem() throws Exception {
        when(itemService.getItemById(1L, 2L)).thenReturn(item(1L));

        mockMvc.perform(get("/items/1").header("X-Sharer-User-Id", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.comments").isArray());
    }

    @Test
    @DisplayName("Возвращает все вещи пользователя")
    void getAllItemsFromUserShouldReturnItems() throws Exception {
        when(itemService.getAllItemsFromUser(1L)).thenReturn(List.of(ItemMapper.toDto(item(1L))));

        mockMvc.perform(get("/items").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("Возвращает найденные вещи")
    void searchShouldReturnMatchingItems() throws Exception {
        when(itemService.getAllAvailableByNameAndDescription("дрель")).thenReturn(List.of(item(1L)));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Вещь 1"));
    }

    @Test
    @DisplayName("Создаёт новую вещь")
    void addNewItemShouldReturnCreatedItem() throws Exception {
        when(itemService.addNewItem(eq(1L), any())).thenReturn(item(1L));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Создаёт комментарий к вещи")
    void commentShouldReturnCreatedComment() throws Exception {
        Comment comment = comment(1L);
        when(itemService.commentItem(any(), eq(2L), eq(1L))).thenReturn(comment);

        CommentRequestDto dto = new CommentRequestDto();
        dto.setText("Текст комментария");

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorName").value("Автор"));
    }

    @Test
    @DisplayName("Обновляет вещь")
    void updateItemShouldReturnUpdatedItem() throws Exception {
        when(itemService.updateItem(eq(1L), any(), eq(2L))).thenReturn(item(2L));

        mockMvc.perform(patch("/items/2")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    @DisplayName("Удаляет вещь")
    void deleteItemShouldCallService() throws Exception {
        mockMvc.perform(delete("/items/2").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());

        verify(itemService).deleteItem(1L, 2L);
    }

    private static Item item(Long id) {
        User owner = new User();
        owner.setId(10L);
        owner.setEmail("owner@example.com");
        owner.setName("Владелец");
        Item item = new Item();
        item.setId(id);
        item.setName("Вещь " + id);
        item.setDescription("Описание вещи");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setComments(List.of());
        return item;
    }

    private static Comment comment(Long id) {
        User author = new User();
        author.setId(2L);
        author.setName("Автор");
        return Comment.builder()
                .id(id)
                .text("Текст комментария")
                .author(author)
                .item(item(1L))
                .created(LocalDateTime.now())
                .build();
    }

    private static ItemDto itemDto() {
        return ItemDto.builder()
                .name("Вещь")
                .description("Описание вещи")
                .available(true)
                .build();
    }
}
