package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.UserItemRequestDto;
import ru.practicum.shareit.request.entity.ItemRequest;

import java.util.List;

public interface ItemRequestService {
    ItemRequest addNewRequest(CreateItemRequestDto request, Long userId);

    List<UserItemRequestDto> getAllRequestsByUser(Long userId);

    List<ItemRequest> getAllActiveRequests(Long userId);

    UserItemRequestDto getRequestById(Long requestId, Long userId);
}
