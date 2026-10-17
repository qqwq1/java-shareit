package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@DisplayName("Тесты REST-контроллера бронирований в шлюзе")
class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    @Test
    @DisplayName("Разбирает состояние и параметры пагинации при получении бронирований")
    void getBookingsShouldParseStateAndPaging() throws Exception {
        when(bookingClient.getBookings(1L, BookingState.WAITING, 0, 10))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1))));

        mockMvc.perform(get("/bookings")
                        .param("state", "waiting")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("Валидирует и передаёт создание бронирования в клиент")
    void bookItemShouldValidateAndDelegateToClient() throws Exception {
        when(bookingClient.bookItem(eq(1L), any())).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookItemRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Отклоняет бронирование с датой начала в прошлом")
    void bookItemShouldRejectPastStart() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto(1L, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Передаёт получение, подтверждение и список бронирований владельца в клиент")
    void getApproveAndOwnerBookingsShouldDelegateToClient() throws Exception {
        when(bookingClient.getBooking(1L, 2L)).thenReturn(ResponseEntity.ok(Map.of("id", 2)));
        when(bookingClient.approve(1L, 2L, true)).thenReturn(ResponseEntity.ok(Map.of("id", 2)));
        when(bookingClient.getOwnerBookings(1L, BookingState.ALL, 0, 10))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("id", 2))));

        mockMvc.perform(get("/bookings/2").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/bookings/2").param("approved", "true").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/bookings/owner").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());
    }

    private static BookItemRequestDto bookItemRequest() {
        return new BookItemRequestDto(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));
    }
}
