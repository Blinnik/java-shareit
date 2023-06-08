package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemDao {
    Item save(Item item);

    Item update(Long itemId, Item item);

    Optional<Item> findOne(Long itemId);

    List<Item> findMany(Long userId);

    List<Item> findManyByTextQuery(String text);

    void delete(Long itemId);

    boolean notExists(Long userId);

    boolean notOwns(Long userId, Long itemId);
}
