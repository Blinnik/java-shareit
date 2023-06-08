package ru.practicum.shareit.item.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.NotOwnerException;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class ItemServiceImpl implements ItemService {
    ItemDao itemDao;
    UserDao userDao;

    @Override
    public Item create(Long userId, Item item) {
        if (userDao.notExists(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        Optional<User> ownerOpt = userDao.findOne(userId);
        ownerOpt.ifPresent(item::setOwner);

        Item createdItem = itemDao.save(item);
        log.info("Был добавлен новый предмет, id={}", createdItem.getId());

        return createdItem;
    }


    @Override
    public Item update(Long userId, Long itemId, Item item) {
        if (userDao.notExists(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        if (itemDao.notExists(itemId)) {
            throw new NotFoundException("Предмет с id " + userId + " не найден");
        }
        item.setId(itemId);

        if (itemDao.notOwns(userId, itemId)) {
            throw new NotOwnerException("Пользователь с id " + userId + " не является " +
                    "владельцем предмета с id " + itemId);
        }

        Item foundItem = getOne(userId, itemId);
        if (item.getName() == null) {
            item.setName(foundItem.getName());
        }
        if (item.getDescription() == null) {
            item.setDescription(foundItem.getDescription());
        }
        if (item.getAvailable() == null) {
            item.setAvailable(foundItem.getAvailable());
        }

        item.setOwner(foundItem.getOwner());

        Item updatedItem = itemDao.update(itemId, item);
        log.info("Предмет с id {} был обновлен", itemId);

        return updatedItem;
    }

    @Override
    public Item getOne(Long userId, Long itemId) {
        // userId тут не используется, его по идее можно убрать, но в ТЗ указано наличие в каждом методе
        // видимо пригодится на будущее
        Item item = itemDao.findOne(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден"));

        log.info("Получен предмет с id {}: {}", itemId, item);

        return item;
    }

    @Override
    public List<Item> getMany(Long userId) {
        if (userDao.notExists(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        List<Item> items = itemDao.findMany(userId);
        log.info("Получен список всех предметов пользователя");

        return items;
    }

    @Override
    public List<Item> getManyByTextQuery(Long userId, String text) {
        if (userDao.notExists(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        List<Item> items = itemDao.findManyByTextQuery(text);
        log.info("Получен список всех предметов по запросу \"" + text + "\"");

        return items;
    }

    @Override
    public void delete(Long userId, Long itemId) {
        if (userDao.notExists(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (itemDao.notExists(itemId)) {
            throw new NotFoundException("Предмет с id " + itemId + " не найден");
        }
        if (itemDao.notOwns(userId, itemId)) {
            throw new NotOwnerException("Пользователь с id " + userId + " не является " +
                    "владельцем предмета с id " + itemId);
        }

        itemDao.delete(itemId);
        log.info("Предмет с id {} был удален", itemId);
    }
}
