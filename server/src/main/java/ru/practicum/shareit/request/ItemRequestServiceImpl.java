package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.entity.Item;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.UserItemRequestDto;
import ru.practicum.shareit.request.entity.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.entity.User;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequest addNewRequest(CreateItemRequestDto request, Long userId) {
        User user = getUserIfExists(userId);

        String description = request.getDescription();
        if (itemRequestRepository.findByOwner_IdAndDescriptionIgnoreCase(userId, description).isPresent()) {
            throw new DuplicateException("Запрос от пользователя с таким описанием уже существует",
                    description,
                    "description");
        }
        return itemRequestRepository.save(new ItemRequest(description, user, LocalDateTime.now()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserItemRequestDto> getAllRequestsByUser(Long userId) {
        User user = getUserIfExists(userId);

        List<ItemRequest> userRequests = itemRequestRepository.findAllByOwner_IdOrderByCreatedDesc(user.getId());
        List<Long> requestIds = userRequests.stream()
                .map(ItemRequest::getId)
                .toList();
        List<Item> offeredItems = itemRepository.findAllByRequest_IdIn(requestIds);
        Map<Long, List<Offer>> requestIdOffers = new HashMap<>();
        offeredItems.forEach(item ->
                requestIdOffers.computeIfAbsent(item.getRequest().getId(), key -> new ArrayList<>())
                        .add(new Offer(item.getId(), item.getName(), item.getOwnerId())));

        return userRequests.stream()
                .map(request -> toUserItemRequestDto(request, requestIdOffers))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequest> getAllActiveRequests(Long userId) {
        User user = getUserIfExists(userId);
        return itemRequestRepository.findAllByOwner_IdNotOrderByCreatedDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserItemRequestDto getRequestById(Long requestId, Long userId) {
        User user = getUserIfExists(userId);
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Не найден запрос с id=" + requestId,
                        requestId.toString(),
                        "requestId"));
        List<Offer> offers = itemRepository.findAllByRequest_IdIn(Collections.singletonList(itemRequest.getId()))
                .stream()
                .map(item -> new Offer(item.getId(), item.getName(), item.getOwnerId()))
                .toList();
        return new UserItemRequestDto(requestId, itemRequest.getDescription(), itemRequest.getCreated(), offers);
    }

    private UserItemRequestDto toUserItemRequestDto(ItemRequest request, Map<Long, List<Offer>> requestIdOffers) {
        return new UserItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getCreated(),
                requestIdOffers.getOrDefault(request.getId(), List.of())
        );
    }

    private User getUserIfExists(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с id " + userId + " не зарегистрирован",
                        userId.toString(),
                        "X-Sharer-User-Id")
        );
    }
}
