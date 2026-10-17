package ru.practicum.shareit.json;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@DisplayName("JSON-тесты DTO")
class DtoJsonTest {
    @Autowired
    private JacksonTester<BookingRequestDto> bookingRequestJson;

    @Autowired
    private JacksonTester<ItemDto> itemJson;

    @Test
    @DisplayName("Десериализует бронирование с датами в ISO-формате")
    void bookingRequestShouldDeserializeIsoLocalDateTime() throws Exception {
        String json = "{\"itemId\":10,\"start\":\"2026-10-12T10:15:30\",\"end\":\"2026-10-12T11:15:30\"}";
        BookingRequestDto dto = bookingRequestJson.parseObject(json);

        assertThat(dto.getItemId()).isEqualTo(10L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2026, 10, 12, 10, 15, 30));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2026, 10, 12, 11, 15, 30));
    }

    @Test
    @DisplayName("Сериализует вещь с вложенными комментариями и датами")
    void itemDtoShouldSerializeNestedCommentsAndDates() throws Exception {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Вещь")
                .available(true)
                .description("Описание вещи")
                .comments(List.of(CommentResponseDto.builder()
                        .id(2L)
                        .text("Текст комментария")
                        .authorName("Автор")
                        .created(LocalDateTime.of(2026, 10, 12, 10, 15, 30))
                        .build()))
                .build();

        assertThat(itemJson.write(dto)).extractingJsonPathStringValue("$.comments[0].created")
                .isEqualTo("2026-10-12T10:15:30");
        assertThat(itemJson.write(dto)).extractingJsonPathStringValue("$.comments[0].authorName")
                .isEqualTo("Автор");
    }
}
