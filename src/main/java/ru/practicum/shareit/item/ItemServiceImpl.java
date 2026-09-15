package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.util.CopyUtil;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final UserService userService;

    private List<Item> getAllItemsByUserId(Long userId) {
        if (!userService.isUserExists(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не зарегистрирован", userId.toString(), "id");
        }
        return repository.findByUserId(userId);
    }

    @Override
    public Item addNewItem(Long userId, ItemDto itemDto) {
        Item item = ItemMapper.toModel(itemDto);
        setOwnerIdIfExist(userId, item);
        return repository.save(item);
    }

    @Override
    public Item updateItem(Long ownerId, ItemDto updatedItemDto, Long itemId) {
        updatedItemDto.setId(itemId);
        Item updatedItem = ItemMapper.toModel(updatedItemDto);
        setOwnerIdIfExist(ownerId, updatedItem);
        Item itemToUpdate = repository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Отсутствует вещь с " + itemId,
                        itemId.toString(), "id"));
        checkGivenOwnerId(itemToUpdate.getOwnerId(), updatedItem.getOwnerId());
        CopyUtil.copyNonNullProperties(updatedItem, itemToUpdate);
        return itemToUpdate;
    }

    @Override
    public Item getItemById(Long itemId, Long userId) {
        Item item = repository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Не найдена вещь с id=" + itemId,
                        itemId.toString(), "id"));
        checkGivenOwnerId(item.getOwnerId(), userId);
        return item;

    }

    @Override
    public void deleteItem(Long userId, Long itemId) {
        repository.deleteByUserIdAndItemId(userId, itemId);
    }

    @Override
    public Collection<Item> getAllItemsFromUser(Long userId) {
        if (userId != null) {
            return getAllItemsByUserId(userId);
        }
        return getAllItems();
    }

    private Collection<Item> getAllItems() {
        return repository.findAll();
    }

    @Override
    public List<Item> getAllAvailableByNameAndDescription(String text) {
        return repository.findAll().stream()
                .filter(item -> {
                    String name = item.getName().toLowerCase(Locale.ROOT);
                    String description = item.getDescription().toLowerCase(Locale.ROOT);

                    return description.contains(text) || name.contains(text);
                })
                .filter(Item::getAvailable)
                .toList();
    }

    private void setOwnerIdIfExist(Long userId, Item item) {
        if (!userService.isUserExists(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не зарегистрирован", userId.toString(), "id");
        }
        item.setOwnerId(userId);
    }

    private void checkGivenOwnerId(Long realOwnerId, Long requestOwnerId) {
        if (!requestOwnerId.equals(realOwnerId)) {
            throw new NotFoundException("Id фактического владельца вещи не совпадает с переданным id=" + requestOwnerId,
                    requestOwnerId.toString(), "ownerId");
        }
    }
}
