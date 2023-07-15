package ru.practicum.shareit.item.service;

import ru.practicum.shareit.common.model.PaginationConfig;
import ru.practicum.shareit.item.model.dto.*;

import java.util.List;

public interface ItemService {
    ItemRequestIdDto create(Long userId, ItemRequestIdDto itemRequestIdDto);

    ItemDto update(Long userId, Long itemId, ItemDto itemDto);

    ItemBookingsAndCommentsDto getById(Long userId, Long itemId);

    List<ItemBookingsDto> getAllByOwnerId(Long ownerId, PaginationConfig paginationConfig);

    List<ItemDto> getAllByTextQuery(Long userId, String text, PaginationConfig paginationConfig);

    void delete(Long userId, Long itemId);

    CommentDto createComment(Long userId, Long itemId, CommentTextDto commentTextDto);
}
