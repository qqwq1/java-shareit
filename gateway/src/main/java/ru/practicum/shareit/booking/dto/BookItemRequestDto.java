package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.handler.Create;
import ru.practicum.shareit.handler.Update;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookItemRequestDto {
    @NotNull(message = "Необходимо указывать id вещи, которую вы желаете забронировать",
            groups = {Create.class})
    private Long itemId;

    @FutureOrPresent(message = "Дата начала бронирования не может быть раньше текущего времени",
            groups = {Create.class, Update.class})
    @NotNull(message = "Необходимо указывать дату начала бронирования",
            groups = {Create.class})
    private LocalDateTime start;

    @Future(message = "Дата окончания бронирования должна быть в будущем",
            groups = {Create.class, Update.class})
    @NotNull(message = "Необходимо указывать дату окончания бронирования",
            groups = {Create.class})
    private LocalDateTime end;
}
