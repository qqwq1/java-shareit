package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.entity.Comment;
import ru.practicum.shareit.item.entity.Item;

import java.util.Collection;
import java.util.List;

public interface ItemService {

    Item addNewItem(Long userId, ItemDto itemdto);

    Item updateItem(Long userId, ItemDto updatedItemDto, Long itemId);

    Item getItemById(Long itemId, Long userId);

    void deleteItem(Long userId, Long itemId);

    Comment commentItem(CommentRequestDto commentRequestDto, Long userId, Long itemId);

    Collection<ItemDto> getAllItemsFromUser(Long userId);

    List<Item> getAllAvailableByNameAndDescription(String text);
}
