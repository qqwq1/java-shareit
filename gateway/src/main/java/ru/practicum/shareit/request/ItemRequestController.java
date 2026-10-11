package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> addNewRequest(@Valid @RequestBody CreateItemRequestDto requestDto,
                                                @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestClient.addNewRequest(requestDto, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllRequestsByUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestClient.getAllRequestsByUser(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestClient.getAllRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @PathVariable Long requestId) {
        return itemRequestClient.getRequestById(requestId, userId);
    }
}
