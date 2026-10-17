package ru.practicum.shareit.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.entity.Booking;
import ru.practicum.shareit.exception.NotFoundException;
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

    @Autowired
    private BookingRepository bookingRepository;

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
    @DisplayName("Отклоняет некорректное создание бронирования")
    void createNewBookingShouldRejectInvalidRequests() {
        Fixture fixture = fixture("oshibka-sozdaniya", "Вещь с ограничениями");
        fixture.item.setAvailable(false);
        itemRepository.save(fixture.item);

        assertThatThrownBy(() -> bookingService.createNewBooking(
                bookingRequest(fixture.item.getId(), LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(1)),
                fixture.booker.getId()))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> bookingService.createNewBooking(bookingRequest(999L), fixture.booker.getId()))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> bookingService.createNewBooking(bookingRequest(fixture.item.getId()), fixture.booker.getId()))
                .isInstanceOf(ValidationException.class);

        fixture.item.setAvailable(true);
        itemRepository.save(fixture.item);

        assertThatThrownBy(() -> bookingService.createNewBooking(bookingRequest(fixture.item.getId()), fixture.owner.getId()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("Отклоняет некорректное подтверждение бронирования")
    void approveShouldRejectInvalidRequests() {
        Fixture fixture = fixture("oshibka-podtverzhdeniya", "Вещь для отказа");
        Booking booking = bookingService.createNewBooking(bookingRequest(fixture.item.getId()), fixture.booker.getId());

        assertThatThrownBy(() -> bookingService.approve(true, fixture.booker.getId(), booking.getId()))
                .isInstanceOf(ValidationException.class);
        assertThat(bookingService.approve(false, fixture.owner.getId(), booking.getId()).getStatus())
                .isEqualTo(BookingStatus.REJECTED);
        assertThatThrownBy(() -> bookingService.approve(true, fixture.owner.getId(), booking.getId()))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> bookingService.approve(true, fixture.owner.getId(), 999L))
                .isInstanceOf(NotFoundException.class);
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
    @DisplayName("Отклоняет просмотр бронирования посторонним пользователем")
    void getBookingShouldRejectStrangerAndMissingBooking() {
        Fixture fixture = fixture("postoronniy", "Вещь для запрета просмотра");
        User stranger = saveUser("stranger-booking@example.com", "Посторонний пользователь");
        Booking booking = bookingService.createNewBooking(bookingRequest(fixture.item.getId()), fixture.booker.getId());

        assertThatThrownBy(() -> bookingService.getBooking(stranger.getId(), booking.getId()))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> bookingService.getBooking(fixture.owner.getId(), 999L))
                .isInstanceOf(NotFoundException.class);
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

    @Test
    @DisplayName("Фильтрует бронирования по всем состояниям")
    void getAllBookingsShouldReturnEveryState() {
        Fixture fixture = fixture("sostoyaniya", "Вещь для состояний");
        LocalDateTime now = LocalDateTime.now();
        Booking past = saveBooking(fixture, BookingStatus.APPROVED, now.minusDays(3), now.minusDays(2));
        Booking current = saveBooking(fixture, BookingStatus.APPROVED, now.minusHours(1), now.plusHours(1));
        Booking future = saveBooking(fixture, BookingStatus.APPROVED, now.plusDays(1), now.plusDays(2));
        Booking waiting = saveBooking(fixture, BookingStatus.WAITING, now.plusDays(3), now.plusDays(4));
        Booking rejected = saveBooking(fixture, BookingStatus.REJECTED, now.plusDays(5), now.plusDays(6));

        assertThat(bookingService.getAllBookingsByUserAndStatus(fixture.booker.getId(), BookingState.PAST))
                .extracting(Booking::getId)
                .containsExactly(past.getId());
        assertThat(bookingService.getAllBookingsByUserAndStatus(fixture.booker.getId(), BookingState.CURRENT))
                .extracting(Booking::getId)
                .containsExactly(current.getId());
        assertThat(bookingService.getAllBookingsByUserAndStatus(fixture.booker.getId(), BookingState.FUTURE))
                .extracting(Booking::getId)
                .contains(future.getId(), waiting.getId(), rejected.getId());
        assertThat(bookingService.getAllBookingsByUserAndStatus(fixture.booker.getId(), BookingState.REJECTED))
                .extracting(Booking::getId)
                .containsExactly(rejected.getId());

        assertThat(bookingService.getAllBookingsByOwnerAndStatus(fixture.owner.getId(), BookingState.PAST))
                .extracting(Booking::getId)
                .containsExactly(past.getId());
        assertThat(bookingService.getAllBookingsByOwnerAndStatus(fixture.owner.getId(), BookingState.CURRENT))
                .extracting(Booking::getId)
                .containsExactly(current.getId());
        assertThat(bookingService.getAllBookingsByOwnerAndStatus(fixture.owner.getId(), BookingState.FUTURE))
                .extracting(Booking::getId)
                .contains(future.getId(), waiting.getId(), rejected.getId());
        assertThat(bookingService.getAllBookingsByOwnerAndStatus(fixture.owner.getId(), BookingState.REJECTED))
                .extracting(Booking::getId)
                .containsExactly(rejected.getId());
    }

    @Test
    @DisplayName("Отклоняет получение бронирований несуществующего пользователя")
    void getAllBookingsShouldRejectMissingUser() {
        assertThatThrownBy(() -> bookingService.getAllBookingsByUserAndStatus(999L, BookingState.ALL))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> bookingService.getAllBookingsByOwnerAndStatus(999L, BookingState.ALL))
                .isInstanceOf(NotFoundException.class);
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
        return bookingRequest(itemId, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));
    }

    private static BookingRequestDto bookingRequest(Long itemId, LocalDateTime start, LocalDateTime end) {
        return BookingRequestDto.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();
    }

    private Booking saveBooking(Fixture fixture, BookingStatus status, LocalDateTime start, LocalDateTime end) {
        return bookingRepository.save(Booking.builder()
                .item(fixture.item)
                .booker(fixture.booker)
                .status(status)
                .start(start)
                .end(end)
                .build());
    }

    private record Fixture(User owner, User booker, Item item) {
    }
}
