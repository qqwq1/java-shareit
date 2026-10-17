package ru.practicum.shareit.booking;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.entity.Booking;

import java.util.List;

public interface BookingService {
    Booking createNewBooking(BookingRequestDto bookingRequestDto, Long bookerId);

    @Transactional
    Booking approve(Boolean approved, Long ownerId, Long bookingId);

    Booking getBooking(Long ownerId, Long bookingId);

    List<Booking> getAllBookingsByUserAndStatus(Long creatorId, BookingState bookingState);

    List<Booking> getAllBookingsByOwnerAndStatus(Long ownerId, BookingState bookingState);
}
