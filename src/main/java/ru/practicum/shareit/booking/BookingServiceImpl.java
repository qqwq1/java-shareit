package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.entity.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.entity.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final UserService userService;
    private final BookingRepository repository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public Booking createNewBooking(BookingRequestDto bookingRequestDto, Long bookerId) {
        LocalDateTime now = LocalDateTime.now();
        if (bookingRequestDto.getEnd().isBefore(now) || bookingRequestDto.getEnd().isBefore(bookingRequestDto.getStart())) {
            throw new ValidationException("Дата конца бронирования не может быть раньше даты начала бронирования и текущего времени",
                    bookingRequestDto.getEnd(),
                    "end");
        }
        if (bookingRequestDto.getStart().isBefore(now)) {
            throw new ValidationException("Дата начала бронирования не может быть раньше текущего времени",
                    bookingRequestDto.getStart(),
                    "start");
        }

        Booking booking = BookingMapper.toEntity(bookingRequestDto);
        Long itemId = bookingRequestDto.getItemId();
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new NotFoundException("Не найдена вещь с id=" + itemId, itemId.toString(), "id"));
        User booker = userService.getUser(bookerId);
        if (!item.getAvailable()) {
            throw new ValidationException("Вещь для бронирования должна быть доступна",
                    false,
                    "available");
        }
        if (bookerId.equals(item.getOwnerId())) {
            throw new ValidationException("Владелец вещи не может арендовать у себя",
                    bookerId,
                    "booker.id");
        }

        booking.setStatus(BookingStatus.WAITING);
        booking.setItem(item);
        booking.setBooker(booker);

        return repository.save(booking);
    }

    @Transactional
    @Override
    public Booking approve(Boolean approved, Long ownerId, Long bookingId) {
        Booking booking = repository.findById(bookingId).orElseThrow(
                () -> new NotFoundException("Не найдено бронирование с id=" + bookingId,
                        bookingId.toString(),
                        "bookingId")
        );
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Подтверждать/отклонять бронирование можно только в статусе WAITING",
                    booking.getStatus(),
                    "booking.status");
        }
        if (!ownerId.equals(booking.getItem().getOwnerId())) {
            throw new ValidationException("Id фактического владельца вещи не созпадает с указанным",
                    ownerId.toString(),
                    "ownerId");
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        return booking;
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getBooking(Long ownerId, Long bookingId) {
        Booking booking = repository.findById(bookingId).orElseThrow(
                () -> new NotFoundException("Не найдено бронирование с id=" + bookingId,
                        bookingId.toString(),
                        "bookingId")
        );
        if (!booking.getBooker().getId().equals(ownerId) && !booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ValidationException("Просмотр информации доступен только владельцу вещи или создателю",
                    ownerId,
                    "X-Sharer-User-Id");
        }
        return booking;
    }

    @Override
    public List<Booking> getAllBookingsByUserAndStatus(Long creatorId, BookingState bookingState) {
        if (!userService.isUserExists(creatorId)) {
            throw new NotFoundException("Пользователь с id=" + creatorId + " не найден",
                    creatorId.toString(),
                    "X-Sharer-User-Id");
        }

        LocalDateTime now = LocalDateTime.now();

        return switch (bookingState) {
            case ALL -> repository.findAllByBooker_IdOrderByStartDesc(creatorId);
            case CURRENT ->
                    repository.findAllByBooker_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(creatorId, now, now);
            case PAST -> repository.findAllByBooker_IdAndEndIsBeforeOrderByStartDesc(creatorId, now);
            case FUTURE -> repository.findAllByBooker_IdAndStartIsAfterOrderByStartDesc(creatorId, now);
            case WAITING -> repository.findAllByBooker_IdAndStatusOrderByStartDesc(creatorId, BookingStatus.WAITING);
            case REJECTED -> repository.findAllByBooker_IdAndStatusOrderByStartDesc(creatorId, BookingStatus.REJECTED);
        };
    }

    @Override
    public List<Booking> getAllBookingsByOwnerAndStatus(Long ownerId, BookingState bookingState) {
        if (!userService.isUserExists(ownerId)) {
            throw new NotFoundException("Пользователь с id=" + ownerId + " не найден",
                    ownerId.toString(),
                    "X-Sharer-User-Id");
        }

        LocalDateTime now = LocalDateTime.now();

        return switch (bookingState) {
            case ALL -> repository.findAllByItem_Owner_IdOrderByStartDesc(ownerId);
            case CURRENT ->
                    repository.findAllByItem_Owner_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(ownerId, now, now);
            case PAST -> repository.findAllByItem_Owner_IdAndEndIsBeforeOrderByStartDesc(ownerId, now);
            case FUTURE -> repository.findAllByItem_Owner_IdAndStartIsAfterOrderByStartDesc(ownerId, now);
            case WAITING -> repository.findAllByItem_Owner_IdAndStatusOrderByStartDesc(ownerId, BookingStatus.WAITING);
            case REJECTED ->
                    repository.findAllByItem_Owner_IdAndStatusOrderByStartDesc(ownerId, BookingStatus.REJECTED);
        };
    }
}
