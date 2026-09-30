package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.validation.Create;

import java.util.List;


@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto create(@RequestHeader("X-Sharer-User-Id") Long bookerId,
                                     @Validated(Create.class) @RequestBody BookingRequestDto bookingRequestDto) {
        return BookingMapper.toResponseDto(bookingService.createNewBooking(bookingRequestDto, bookerId));
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approve(@RequestParam(name = "approved") boolean approved,
                                      @RequestHeader("X-Sharer-User-Id") Long ownerId,
                                      @PathVariable(name = "bookingId") Long bookingId) {
        return BookingMapper.toResponseDto(bookingService.approve(approved, ownerId, bookingId));
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBooking(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                         @PathVariable(name = "bookingId") Long bookingId) {
        return BookingMapper.toResponseDto(bookingService.getBooking(ownerId, bookingId));
    }

    @GetMapping
    public List<BookingResponseDto> getAllBookingsByState(
            @RequestHeader("X-Sharer-User-Id") Long creatorId,
            @RequestParam(name = "state", defaultValue = "ALL") String state) {
        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            bookingState = BookingState.ALL;
        }
        return bookingService.getAllBookingsByUserAndStatus(creatorId, bookingState).stream()
                .map(BookingMapper::toResponseDto)
                .toList();
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getAllBookingsByStateAndItemsOwner(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @RequestParam(name = "state", defaultValue = "ALL") String state) {
        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            bookingState = BookingState.ALL;
        }
        return bookingService.getAllBookingsByOwnerAndStatus(ownerId, bookingState).stream()
                .map(BookingMapper::toResponseDto)
                .toList();
    }
}
