package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.dto.*;

import java.util.List;

public interface ItemService {
    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, Long itemId, ItemDto itemDto);

    ItemBookingsAndCommentsDto getById(Long userId, Long itemId);

    List<ItemBookingsDto> getAllByOwnerId(Long ownerId);

    List<ItemDto> getAllByTextQuery(Long userId, String text);

    void delete(Long userId, Long itemId);

    CommentDto createComment(Long userId, Long itemId, CommentTextDto commentTextDto);
}
