package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.CommonItemRequestDto;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.UserItemRequestDto;
import ru.practicum.shareit.request.entity.ItemRequest;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public CommonItemRequestDto addNewRequest(@RequestBody CreateItemRequestDto requestDto,
                                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        ItemRequest itemRequest = itemRequestService.addNewRequest(requestDto, userId);
        return new CommonItemRequestDto(itemRequest.getId(), itemRequest.getDescription(), itemRequest.getCreated());
    }

    @GetMapping
    public List<UserItemRequestDto> getAllRequestsByUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getAllRequestsByUser(userId);
    }

    @GetMapping("/all")
    public List<CommonItemRequestDto> getAllRequestsASC(@RequestHeader("X-Sharer-User-Id") Long userId) {
        List<ItemRequest> itemRequestList = itemRequestService.getAllActiveRequests(userId);
        return itemRequestList.stream()
                .map(itemRequest -> new CommonItemRequestDto(itemRequest.getId(), itemRequest.getDescription(), itemRequest.getCreated()))
                .toList();
    }

    @GetMapping("/{requestId}")
    public UserItemRequestDto getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable Long requestId) {
        return itemRequestService.getRequestById(requestId, userId);
    }
}
