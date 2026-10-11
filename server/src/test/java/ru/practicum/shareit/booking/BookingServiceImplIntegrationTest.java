package ru.practicum.shareit.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.entity.Booking;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.entity.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.entity.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Интеграционные тесты сервиса бронирований")
class BookingServiceImplIntegrationTest {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    @DisplayName("Создаёт бронирование в статусе ожидания")
    void createNewBookingShouldPersistWaitingBooking() {
        Fixture fixture = fixture("sozdanie", "Вещь для бронирования");

        Booking booking = bookingService.createNewBooking(bookingRequest(fixture.item.getId()), fixture.booker.getId());

        assertThat(booking.getId()).isNotNull();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(booking.getItem().getId()).isEqualTo(fixture.item.getId());
    }

    @Test
    @DisplayName("Меняет статус бронирования только владельцем вещи")
    void approveShouldChangeStatusOnlyByOwner() {
        Fixture fixture = fixture("podtverzhdenie", "Вещь для подтверждения");
        Booking booking = bookingService.createNewBooking(bookingRequest(fixture.item.getId()), fixture.booker.getId());

        Booking approved = bookingService.approve(true, fixture.owner.getId(), booking.getId());

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThatThrownBy(() -> bookingService.approve(false, fixture.booker.getId(), booking.getId()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("Позволяет получить бронирование владельцу и арендатору")
    void getBookingShouldBeAvailableForBookerAndOwner() {
        Fixture fixture = fixture("poluchenie", "Вещь для просмотра");
        Booking booking = bookingService.createNewBooking(bookingRequest(fixture.item.getId()), fixture.booker.getId());

        assertThat(bookingService.getBooking(fixture.booker.getId(), booking.getId()).getId()).isEqualTo(booking.getId());
        assertThat(bookingService.getBooking(fixture.owner.getId(), booking.getId()).getId()).isEqualTo(booking.getId());
    }

    @Test
    @DisplayName("Фильтрует бронирования по пользователю и владельцу")
    void getAllBookingsShouldFilterByUserAndOwner() {
        Fixture fixture = fixture("spisok", "Вещь для списка");
        Booking booking = bookingService.createNewBooking(bookingRequest(fixture.item.getId()), fixture.booker.getId());
        bookingService.approve(true, fixture.owner.getId(), booking.getId());

        assertThat(bookingService.getAllBookingsByUserAndStatus(fixture.booker.getId(), BookingState.ALL))
                .extracting(Booking::getId)
                .containsExactly(booking.getId());
        assertThat(bookingService.getAllBookingsByOwnerAndStatus(fixture.owner.getId(), BookingState.WAITING))
                .isEmpty();
    }

    private Fixture fixture(String suffix, String itemName) {
        User owner = saveUser("owner-" + suffix + "@example.com", "Владелец " + itemName.toLowerCase());
        User booker = saveUser("booker-" + suffix + "@example.com", "Арендатор " + itemName.toLowerCase());
        Item item = new Item();
        item.setName(itemName);
        item.setDescription("Описание вещи");
        item.setAvailable(true);
        item.setOwner(owner);
        return new Fixture(owner, booker, itemRepository.save(item));
    }

    private User saveUser(String email, String name) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    private static BookingRequestDto bookingRequest(Long itemId) {
        return BookingRequestDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();
    }

    private record Fixture(User owner, User booker, Item item) {
    }
}
