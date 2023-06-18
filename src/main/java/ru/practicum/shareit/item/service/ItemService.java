package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item create(Long userId, Item item);

    Item update(Long userId, Long itemId, Item item);

    Item getOne(Long userId, Long itemId);

    List<Item> getMany(Long ownerId);

    List<Item> getManyByTextQuery(Long userId, String text);

    void delete(Long userId, Long itemId);
}
