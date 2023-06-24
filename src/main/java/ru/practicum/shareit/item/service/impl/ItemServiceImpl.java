package ru.practicum.shareit.item.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.NotOwnerException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.QItem;
import ru.practicum.shareit.item.model.dto.*;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class ItemServiceImpl implements ItemService {
    ItemRepository itemRepository;
    UserRepository userRepository;
    BookingRepository bookingRepository;
    CommentRepository commentRepository;

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);

        Item createdItem = itemRepository.save(item);
        log.info("Был добавлен новый предмет, id={}", createdItem.getId());

        return ItemMapper.toItemDto(createdItem);
    }


    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        Item foundItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + userId + " не найден"));

        if (!Objects.equals(foundItem.getOwner().getId(), userId)) {
            throw new NotOwnerException("Пользователь с id " + userId + " не является " +
                    "владельцем предмета с id " + itemId);
        }

        Item item = ItemMapper.toItem(itemDto);
        item.setId(itemId);
        item.setOwner(foundItem.getOwner());

        if (item.getName() == null) {
            item.setName(foundItem.getName());
        }
        if (item.getDescription() == null) {
            item.setDescription(foundItem.getDescription());
        }
        if (item.getAvailable() == null) {
            item.setAvailable(foundItem.getAvailable());
        }

        Item updatedItem = itemRepository.save(item);
        log.info("Предмет с id {} был обновлен", itemId);

        return ItemMapper.toItemDto(updatedItem);
    }

    @Transactional(readOnly = true)
    @Override
    public ItemBookingsAndCommentsDto getById(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден"));

        List<Booking> bookings = bookingRepository.findAllAcceptedByItemId(itemId);
        List<Comment> comments = commentRepository.findAllByItemId(itemId);

        if (!Objects.equals(item.getOwner().getId(), userId)) {
            return ItemMapper.toItemBookingsAndCommentsDto(item, comments);
        }

        ItemBookingsAndCommentsDto itemBookingsAndCommentsDto =
                ItemMapper.toItemBookingsAndCommentsDto(item, bookings, comments);
        log.info("Получен предмет с id {}: {}", itemId, itemBookingsAndCommentsDto);

        return itemBookingsAndCommentsDto;
    }

    @Override
    public List<ItemBookingsDto> getAllByOwnerId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        List<Item> items = itemRepository.findAllByOwnerId(userId);

        List<ItemBookingsDto> itemBookingsDtos = new ArrayList<>();
        for (Item item : items) {
            List<Booking> bookings = bookingRepository.findAllAcceptedByItemId(item.getId());
            ItemBookingsDto itemBookingsDto = ItemMapper.toItemBookingsDto(item, bookings);
            itemBookingsDtos.add(itemBookingsDto);
        }
        log.info("Получен список всех предметов пользователя");

        return itemBookingsDtos;
    }

    @Override
    public List<ItemDto> getAllByTextQuery(Long userId, String text) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        if (text.isEmpty()) {
            return Collections.emptyList();
        }

        BooleanExpression byAvailableTrue = QItem.item.available.isTrue();
        BooleanExpression byNameOrDescriptionContainingText = QItem.item.name.containsIgnoreCase(text)
                .or(QItem.item.description.containsIgnoreCase(text));

        List<Item> items = (List<Item>) itemRepository.findAll(byAvailableTrue.and(byNameOrDescriptionContainingText));

        List<ItemDto> itemDtos = items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        log.info("Получен список всех предметов по запросу \"" + text + "\"");

        return itemDtos;
    }

    @Override
    public void delete(Long userId, Long itemId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (!itemRepository.existsById(itemId)) {
            throw new NotFoundException("Предмет с id " + itemId + " не найден");
        }
        if (notOwns(userId, itemId)) {
            throw new NotOwnerException("Пользователь с id " + userId + " не является " +
                    "владельцем предмета с id " + itemId);
        }

        itemRepository.deleteById(itemId);
        log.info("Предмет с id {} был удален", itemId);
    }

    @Override
    public CommentDto createComment(Long userId, Long itemId, CommentTextDto commentTextDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + userId + " не найден"));

        if (bookingRepository.countAllPrevious(itemId, userId) < 1) {
            throw new NotAvailableException("Пользователь с id " + userId +
                    " раньше не бронировал предмет с id " + itemId);
        }

        Comment comment = ItemMapper.toComment(userId, commentTextDto);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());
        comment.setItem(item);

        CommentDto commentDto = ItemMapper.toCommentDto(commentRepository.save(comment));
        log.info("Был добавлен новый комментарий, id {}", commentDto.getId());

        return commentDto;
    }

    private boolean notOwns(Long userId, Long itemId) {
        Optional<Item> itemOpt = itemRepository.findById(itemId);
        return itemOpt.map(item -> !Objects.equals(item.getOwner().getId(), userId)).orElse(true);
    }
}
