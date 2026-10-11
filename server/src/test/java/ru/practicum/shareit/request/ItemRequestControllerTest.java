package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.UserItemRequestDto;
import ru.practicum.shareit.request.entity.ItemRequest;
import ru.practicum.shareit.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
@DisplayName("Тесты REST-контроллера запросов вещей на сервере")
class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    @DisplayName("Создаёт новый запрос вещи")
    void addNewRequestShouldReturnCreatedRequest() throws Exception {
        when(itemRequestService.addNewRequest(any(), eq(1L))).thenReturn(request(1L));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Возвращает запросы пользователя")
    void getAllRequestsByUserShouldReturnRequests() throws Exception {
        when(itemRequestService.getAllRequestsByUser(1L))
                .thenReturn(List.of(userRequest(1L)));

        mockMvc.perform(get("/requests").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Нужна вещь"));
    }

    @Test
    @DisplayName("Возвращает активные запросы")
    void getAllRequestsShouldReturnActiveRequests() throws Exception {
        when(itemRequestService.getAllActiveRequests(1L)).thenReturn(List.of(request(2L)));

        mockMvc.perform(get("/requests/all").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    @DisplayName("Возвращает запрос по идентификатору")
    void getRequestByIdShouldReturnRequest() throws Exception {
        when(itemRequestService.getRequestById(2L, 1L)).thenReturn(userRequest(2L));

        mockMvc.perform(get("/requests/2").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    private static CreateItemRequestDto requestDto() {
        CreateItemRequestDto dto = new CreateItemRequestDto();
        dto.setDescription("Нужна вещь");
        return dto;
    }

    private static ItemRequest request(Long id) {
        User owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@example.com");
        owner.setName("Владелец");
        ItemRequest request = new ItemRequest("Нужна вещь", owner, LocalDateTime.now());
        request.setId(id);
        return request;
    }

    private static UserItemRequestDto userRequest(Long id) {
        return new UserItemRequestDto(id, "Нужна вещь", LocalDateTime.now(), List.of(new Offer(1L, "Вещь", 2L)));
    }
}
