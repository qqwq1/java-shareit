package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.handler.Create;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;


@RequiredArgsConstructor
@RequestMapping("/items")
@RestController
public class ItemController {
    private final ItemClient client;

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> get(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return client.getById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItemsFromUser(
            @RequestHeader(name = "X-Sharer-User-Id") Long userId) {
        return client.getAllItemsFromUser(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchAllAvailableByNameAndDescription(@RequestParam Map<String, Object> params,
                                                                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        return client.getAllAvailableByNameAndDescription(userId, params);
    }

    @PostMapping
    public ResponseEntity<Object> addNewItem(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                             @Validated(Create.class) @RequestBody ItemDto itemDto) {
        return client.addNewItem(ownerId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> comment(@Validated @RequestBody CommentRequestDto commentRequestDto,
                                          @RequestHeader("X-Sharer-User-Id") Long userId,
                                          @PathVariable Long itemId) {
        return client.commentItem(commentRequestDto, userId, itemId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                             @RequestBody ItemDto itemDto,
                                             @PathVariable Long itemId) {
        return client.updateItem(ownerId, itemDto, itemId);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable(name = "itemId") long itemId) {
        return client.deleteItem(userId, itemId);
    }
}
