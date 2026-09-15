package ru.practicum.shareit.item;


import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.util.CopyUtil;


public class ItemMapper {
    public static ItemDto toDto(Item item) {
        ItemDto itemDto = new ItemDto();
        CopyUtil.copyNonNullProperties(item, itemDto);
        return itemDto;
    }

    public static Item toModel(ItemDto itemDto) {
        Item item = new Item();
        CopyUtil.copyNonNullProperties(itemDto, item);
        return item;
    }

}
