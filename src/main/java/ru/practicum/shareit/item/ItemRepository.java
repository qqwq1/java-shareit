package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

interface ItemRepository {

    List<Item> findByUserId(long userId);

    Optional<Item> findById(Long id);

    Collection<Item> findAll();

    Item save(Item item);

    void deleteByUserIdAndItemId(long userId, long itemId);
}