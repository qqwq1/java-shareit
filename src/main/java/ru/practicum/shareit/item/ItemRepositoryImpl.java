package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public List<Item> findByUserId(long userId) {
        return items.values().stream()
                .filter(item -> item.getOwnerId() == userId)
                .toList();
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public Collection<Item> findAll() {
        return items.values();
    }

    @Override
    public Item save(Item item) {
        item.setId(getNextId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public void deleteByUserIdAndItemId(long userId, long itemId) {

    }


    private Long getNextId() {
        return items.values().stream()
                .map(Item::getId)
                .max(Long::compare).orElse(0L) + 1;

    }
}
