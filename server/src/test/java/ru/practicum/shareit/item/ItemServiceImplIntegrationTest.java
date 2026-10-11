package ru.practicum.shareit.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.entity.Booking;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.entity.Comment;
import ru.practicum.shareit.item.entity.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.entity.User;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Интеграционные тесты сервиса вещей")
class ItemServiceImplIntegrationTest {
    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @DisplayName("Создаёт, обновляет и получает вещь через базу данных")
    void addUpdateAndGetItemShouldUseDatabase() {
        User owner = saveUser("owner-item@example.com", "Владелец дрели");
        ItemDto created = itemDto("Дрель", "Сетевая дрель", true);

        Item item = itemService.addNewItem(owner.getId(), created);
        ItemDto patch = ItemDto.builder().description("Аккумуляторная дрель").build();
        Item updated = itemService.updateItem(owner.getId(), patch, item.getId());

        assertThat(itemService.getItemById(item.getId(), owner.getId()).getName()).isEqualTo("Дрель");
        assertThat(updated.getDescription()).isEqualTo("Аккумуляторная дрель");
    }

    @Test
    @DisplayName("Возвращает вещи владельца с последним и следующим бронированием")
    void getAllItemsFromUserShouldIncludeLastAndNextBookings() {
        User owner = saveUser("owner-bookings@example.com", "Владелец пилы");
        User booker = saveUser("booker-bookings@example.com", "Арендатор пилы");
        Item item = itemService.addNewItem(owner.getId(), itemDto("Пила", "Ручная пила", true));
        saveBooking(item, booker, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
        saveBooking(item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        Collection<ItemDto> items = itemService.getAllItemsFromUser(owner.getId());

        ItemDto dto = items.iterator().next();
        assertThat(dto.getLastBooking()).isNotNull();
        assertThat(dto.getNextBooking()).isNotNull();
        assertThat(dto.getLastBooking().getBookerId()).isEqualTo(booker.getId());
    }

    @Test
    @DisplayName("Ищет только доступные вещи по названию или описанию")
    void searchShouldReturnOnlyAvailableMatchingItems() {
        User owner = saveUser("owner-search@example.com", "Владелец фотоаппарата");
        itemService.addNewItem(owner.getId(), itemDto("Фотоаппарат", "Цифровая камера", true));
        itemService.addNewItem(owner.getId(), itemDto("Штатив", "Штатив для камеры", false));

        assertThat(itemService.getAllAvailableByNameAndDescription("камера"))
                .extracting(Item::getName)
                .containsExactly("Фотоаппарат");
    }

    @Test
    @DisplayName("Разрешает комментарий после завершённого подтверждённого бронирования")
    void commentItemShouldRequireFinishedApprovedBooking() {
        User owner = saveUser("owner-comment@example.com", "Владелец велосипеда");
        User booker = saveUser("booker-comment@example.com", "Арендатор велосипеда");
        Item item = itemService.addNewItem(owner.getId(), itemDto("Велосипед", "Городской велосипед", true));
        saveBooking(item, booker, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
        CommentRequestDto request = new CommentRequestDto();
        request.setText("Хорошая вещь");

        Comment comment = itemService.commentItem(request, booker.getId(), item.getId());

        assertThat(comment.getId()).isNotNull();
        assertThat(comment.getAuthor().getId()).isEqualTo(booker.getId());
        assertThat(itemService.getItemById(item.getId(), booker.getId()).getComments()).hasSize(1);
    }

    @Test
    @DisplayName("Запрещает владельцу оставлять комментарий к своей вещи")
    void commentItemShouldRejectOwner() {
        User owner = saveUser("owner-comment-reject@example.com", "Владелец палатки");
        Item item = itemService.addNewItem(owner.getId(), itemDto("Палатка", "Туристическая палатка", true));
        CommentRequestDto request = new CommentRequestDto();
        request.setText("Комментарий владельца");

        assertThatThrownBy(() -> itemService.commentItem(request, owner.getId(), item.getId()))
                .isInstanceOf(ValidationException.class);
    }

    private User saveUser(String email, String name) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    private Booking saveBooking(Item item, User booker, LocalDateTime start, LocalDateTime end) {
        return bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .start(start)
                .end(end)
                .build());
    }

    private static ItemDto itemDto(String name, String description, boolean available) {
        return ItemDto.builder()
                .name(name)
                .description(description)
                .available(available)
                .build();
    }
}
