package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.validation.Create;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDto {
    private Long id;

    @NotNull(message = "Необходимо указывать id вещи, которую вы желаете забронировать",
            groups = {Create.class})
    private Long itemId;

    @NotNull(message = "Необходимо указывать дату начала бронирования",
            groups = {Create.class})
    private LocalDateTime start;

    @NotNull(message = "Необходимо указывать дату окончания бронирования",
            groups = {Create.class})
    private LocalDateTime end;
}
