package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.entity.Booking;
import ru.practicum.shareit.item.entity.Item;
import ru.practicum.shareit.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@DisplayName("Тесты REST-контроллера бронирований на сервере")
class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    @DisplayName("Создаёт бронирование")
    void createShouldReturnBooking() throws Exception {
        when(bookingService.createNewBooking(any(), eq(2L))).thenReturn(booking(1L));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Подтверждает бронирование")
    void approveShouldReturnApprovedBooking() throws Exception {
        when(bookingService.approve(true, 1L, 2L)).thenReturn(booking(2L));

        mockMvc.perform(patch("/bookings/2")
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("Возвращает бронирование по идентификатору")
    void getBookingShouldReturnBooking() throws Exception {
        when(bookingService.getBooking(1L, 2L)).thenReturn(booking(2L));

        mockMvc.perform(get("/bookings/2").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    @DisplayName("Возвращает бронирования пользователя по состоянию")
    void getAllBookingsByStateShouldReturnUserBookings() throws Exception {
        when(bookingService.getAllBookingsByUserAndStatus(2L, BookingState.WAITING))
                .thenReturn(List.of(booking(1L)));

        mockMvc.perform(get("/bookings")
                        .param("state", "WAITING")
                        .header("X-Sharer-User-Id", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("Возвращает бронирования владельца по состоянию")
    void getAllBookingsByStateAndItemsOwnerShouldReturnOwnerBookings() throws Exception {
        when(bookingService.getAllBookingsByOwnerAndStatus(1L, BookingState.ALL))
                .thenReturn(List.of(booking(1L)));

        mockMvc.perform(get("/bookings/owner").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    private static Booking booking(Long id) {
        User owner = user(1L, "owner@example.com");
        User booker = user(2L, "booker@example.com");
        Item item = new Item();
        item.setId(10L);
        item.setName("Вещь");
        item.setDescription("Описание вещи");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setComments(List.of());
        return Booking.builder()
                .id(id)
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();
    }

    private static User user(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setName("Пользователь " + id);
        return user;
    }

    private static BookingRequestDto bookingRequest() {
        return BookingRequestDto.builder()
                .itemId(10L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();
    }
}
