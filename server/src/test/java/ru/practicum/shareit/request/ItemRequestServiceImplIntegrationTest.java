package ru.practicum.shareit.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.entity.Item;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.UserItemRequestDto;
import ru.practicum.shareit.request.entity.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.entity.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Интеграционные тесты сервиса запросов вещей")
class ItemRequestServiceImplIntegrationTest {
    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    @DisplayName("Создаёт запрос и отклоняет повтор с тем же описанием от пользователя")
    void addNewRequestShouldPersistAndRejectDuplicateForSameUser() {
        User user = saveUser("request-owner@example.com", "Автор запроса");
        CreateItemRequestDto requestDto = requestDto("Нужна лестница");

        ItemRequest saved = itemRequestService.addNewRequest(requestDto, user.getId());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDescription()).isEqualTo("Нужна лестница");
        assertThatThrownBy(() -> itemRequestService.addNewRequest(requestDto("нужна ЛЕСТНИЦА"), user.getId()))
                .isInstanceOf(DuplicateException.class);
    }

    @Test
    @DisplayName("Возвращает запросы пользователя вместе с предложенными вещами")
    void getAllRequestsByUserShouldIncludeOfferedItems() {
        User requester = saveUser("requester@example.com", "Автор запроса молотка");
        User owner = saveUser("offer-owner@example.com", "Владелец молотка");
        ItemRequest request = itemRequestService.addNewRequest(requestDto("Нужен молоток"), requester.getId());
        saveOfferedItem(owner, request);

        List<UserItemRequestDto> requests = itemRequestService.getAllRequestsByUser(requester.getId());

        assertThat(requests).hasSize(1);
        assertThat(requests.getFirst().getItems()).hasSize(1);
        assertThat(requests.getFirst().getItems().getFirst().getOwnerId()).isEqualTo(owner.getId());
    }

    @Test
    @DisplayName("Возвращает активные запросы других пользователей")
    void getAllActiveRequestsShouldExcludeOwnRequests() {
        User requester = saveUser("active-requester@example.com", "Автор собственного запроса");
        User other = saveUser("active-other@example.com", "Автор чужого запроса");
        ItemRequest own = itemRequestService.addNewRequest(requestDto("Собственный запрос"), requester.getId());
        ItemRequest active = itemRequestService.addNewRequest(requestDto("Чужой запрос"), other.getId());

        assertThat(itemRequestService.getAllActiveRequests(requester.getId()))
                .extracting(ItemRequest::getId)
                .contains(active.getId())
                .doesNotContain(own.getId());
    }

    @Test
    @DisplayName("Возвращает запрос по идентификатору вместе с предложениями")
    void getRequestByIdShouldReturnRequestWithOffers() {
        User requester = saveUser("by-id-requester@example.com", "Автор запроса ключа");
        User owner = saveUser("by-id-owner@example.com", "Владелец ключа");
        ItemRequest request = itemRequestService.addNewRequest(requestDto("Нужен гаечный ключ"), requester.getId());
        Item item = saveOfferedItem(owner, request);

        UserItemRequestDto dto = itemRequestService.getRequestById(request.getId(), owner.getId());

        assertThat(dto.getId()).isEqualTo(request.getId());
        assertThat(dto.getItems()).extracting(Offer::getId).containsExactly(item.getId());
    }

    private Item saveOfferedItem(User owner, ItemRequest request) {
        Item item = new Item();
        item.setName("Предложенная вещь");
        item.setDescription("Подходит под запрос");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);
        return itemRepository.save(item);
    }

    private User saveUser(String email, String name) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    private static CreateItemRequestDto requestDto(String description) {
        CreateItemRequestDto dto = new CreateItemRequestDto();
        dto.setDescription(description);
        return dto;
    }
}
