package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.validation.Create;
import ru.practicum.shareit.validation.Update;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;


@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
class ItemController {
    private final ItemService itemService;

    @GetMapping("/{itemId}")
    public ItemDto get(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return ItemMapper.toDto(itemService.getItemById(itemId, userId));
    }

    @GetMapping()
    public Collection<ItemDto> getAllItemsFromUser(@RequestHeader(required = false,
            name = "X-Sharer-User-Id") Long userId) {
        return itemService.getAllItemsFromUser(userId).stream()
                .map(ItemMapper::toDto)
                .toList();
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchAllAvailableByNameAndDescription(@RequestParam String text,
                                                                      @RequestHeader("X-Sharer-User-Id") Long userId) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return itemService.getAllAvailableByNameAndDescription(text.toLowerCase(Locale.ROOT)).stream()
                .map(ItemMapper::toDto)
                .toList();
    }

    @PostMapping
    public ItemDto addNewItem(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                              @Validated(Create.class) @RequestBody ItemDto itemDto) {
        return ItemMapper.toDto(itemService.addNewItem(ownerId, itemDto));
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                              @Validated(Update.class) @RequestBody ItemDto itemDto,
                              @PathVariable Long itemId) {
        return ItemMapper.toDto(itemService.updateItem(ownerId, itemDto, itemId));
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@RequestHeader("X-Sharer-User-Id") long userId,
                           @PathVariable(name = "itemId") long itemId) {
        itemService.deleteItem(userId, itemId);
    }
}