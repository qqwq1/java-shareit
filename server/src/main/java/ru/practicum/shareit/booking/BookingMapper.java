package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.entity.Booking;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.UserMapper;

public class BookingMapper {
    public static BookingResponseDto toResponseDto(Booking booking) {
        return BookingResponseDto.builder()
                .id(booking.getId())
                .item(ItemMapper.toDto(booking.getItem()))
                .booker(UserMapper.toDto(booking.getBooker()))
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .build();
    }

    public static Booking toEntity(BookingRequestDto bookingRequestDto) {
        return Booking.builder()
                .start(bookingRequestDto.getStart())
                .end(bookingRequestDto.getEnd())
                .build();
    }
}
