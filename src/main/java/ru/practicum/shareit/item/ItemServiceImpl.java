package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.entity.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.entity.Comment;
import ru.practicum.shareit.item.entity.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.entity.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final CommentRepository commentRepository;

    private List<Item> getAllItemsByUserId(Long userId) {
        if (!userService.isUserExists(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не зарегистрирован", userId.toString(), "id");
        }
        return repository.findByOwner_Id(userId);
    }

    @Override
    @Transactional
    public Item addNewItem(Long userId, ItemDto itemDto) {
        Item item = ItemMapper.toEntity(itemDto);
        setOwnerIfExist(userId, item);
        return repository.save(item);
    }

    @Override
    @Transactional
    public Item updateItem(Long ownerId, ItemDto updatedItemDto, Long itemId) {
        updatedItemDto.setId(itemId);
        Item updatedItem = getOrElseThrow(itemId);
        checkGivenOwnerId(updatedItem.getOwnerId(), ownerId);
        ItemMapper.updateEntity(updatedItemDto, updatedItem);
        return updatedItem;
    }

    @Override
    public Item getItemById(Long itemId, Long userId) {
        return repository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Не найдена вещь с id=" + itemId,
                        itemId.toString(), "id"));

    }

    @Override
    public void deleteItem(Long userId, Long itemId) {
        repository.deleteByOwner_IdAndId(userId, itemId);
    }

    @Override
    @Transactional
    public Comment commentItem(CommentRequestDto commentRequestDto, Long userId, Long itemId) {
        if (!userService.isUserExists(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не зарегистрирован", userId.toString(), "id");
        }

        boolean canComment = bookingRepository.existsByItem_IdAndBooker_IdAndStatusAndEndIsBefore(
                itemId,
                userId,
                BookingStatus.APPROVED,
                LocalDateTime.now()
        );

        Item item = getOrElseThrow(itemId);
        if (!canComment || userId.equals(item.getOwnerId())) {
            throw new ValidationException("Вы не можете оставить отзыв",
                    userId,
                    "authorId");
        }

        return commentRepository.save(Comment.builder()
                .text(commentRequestDto.getText())
                .author(userService.getUser(userId))
                .item(item)
                .created(LocalDateTime.now())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ItemDto> getAllItemsFromUser(Long userId) {
        if (userId != null) {
            List<Item> items = getAllItemsByUserId(userId);
            if (items.isEmpty()) {
                return List.of();
            }

            LocalDateTime now = LocalDateTime.now();
            List<Long> itemIds = items.stream()
                    .map(Item::getId)
                    .toList();

            Map<Long, ItemBookingDto> lastBookingsByItemId = toBookingDtoByItemId(
                    bookingRepository.findLastBookingsByItemIds(
                            itemIds,
                            BookingStatus.APPROVED,
                            now
                    )
            );
            Map<Long, ItemBookingDto> nextBookingsByItemId = toBookingDtoByItemId(
                    bookingRepository.findNextBookingsByItemIds(
                            itemIds,
                            BookingStatus.APPROVED,
                            now
                    )
            );

            return items.stream()
                    .map(item -> {
                        ItemDto itemDto = ItemMapper.toDto(item);
                        itemDto.setLastBooking(lastBookingsByItemId.get(item.getId()));
                        itemDto.setNextBooking(nextBookingsByItemId.get(item.getId()));
                        return itemDto;
                    })
                    .toList();
        }
        return getAllItems().stream()
                .map(ItemMapper::toDto)
                .toList();
    }

    private Collection<Item> getAllItems() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Item> getAllAvailableByNameAndDescription(String text) {
        return repository
                .findAllByAvailableTrueAndNameContainingIgnoreCaseOrAvailableTrueAndDescriptionContainingIgnoreCase(
                        text,
                        text
                );
    }

    private void setOwnerIfExist(Long userId, Item item) {
        User owner = userService.getUser(userId);
        item.setOwner(owner);
    }

    private void checkGivenOwnerId(Long realOwnerId, Long requestOwnerId) {
        if (!requestOwnerId.equals(realOwnerId)) {
            throw new NotFoundException("Id фактического владельца вещи не совпадает с переданным id=" + requestOwnerId,
                    requestOwnerId.toString(), "ownerId");
        }
    }

    private Item getOrElseThrow(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new NotFoundException("Не найдена вещь с id=" + id, id.toString(), "id"));
    }


    private Map<Long, ItemBookingDto> toBookingDtoByItemId(List<Booking> bookings) {
        return bookings.stream()
                .collect(Collectors.toMap(
                        booking -> booking.getItem().getId(),
                        this::toItemBookingDto
                ));
    }

    private ItemBookingDto toItemBookingDto(Booking booking) {
        return new ItemBookingDto(
                booking.getId(),
                booking.getBooker().getId(),
                booking.getStart(),
                booking.getEnd()
        );
    }
}
