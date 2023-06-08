package ru.practicum.shareit.item.dao.impl;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Service
public class ItemDaoImpl implements ItemDao {
    final Map<Long, Item> itemsById = new HashMap<>();
    Long lastId = 1L;

    @Override
    public Item save(Item item) {
        item.setId(lastId++);
        itemsById.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Long itemId, Item item) {
        itemsById.put(itemId, item);
        return item;
    }

    @Override
    public Optional<Item> findOne(Long itemId) {
        return Optional.ofNullable(itemsById.get(itemId));
    }

    @Override
    public List<Item> findMany(Long userId) {
        List<Item> userItems = new ArrayList<>();

        for (Item item : itemsById.values()) {
            if (Objects.equals(item.getOwner().getId(), userId)) {
                userItems.add(item);
            }
        }

        return userItems;
    }

    @Override
    public List<Item> findManyByTextQuery(String text) {
        if (text.isEmpty()) {
            return Collections.emptyList();
        }

        List<Item> foundItems = new ArrayList<>();
        String lowerCaseText = text.toLowerCase();

        for (Item item : itemsById.values()) {
            if (!item.getAvailable()) {
                continue;
            }

            if (item.getName().toLowerCase().contains(lowerCaseText)
                    || item.getDescription().toLowerCase().contains(lowerCaseText)) {
                foundItems.add(item);
            }
        }

        return foundItems;
    }

    @Override
    public void delete(Long itemId) {
        itemsById.remove(itemId);
    }

    @Override
    public boolean notExists(Long itemId) {
        return findOne(itemId).isEmpty();
    }

    @Override
    public boolean notOwns(Long userId, Long itemId) {
        Optional<Item> itemOpt = findOne(itemId);
        if (itemOpt.isPresent()) {
            User owner = itemOpt.get().getOwner();
            return !Objects.equals(owner.getId(), userId);
        }
        return true;
    }
}
