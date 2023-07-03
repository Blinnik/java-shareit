package ru.practicum.shareit.request.service;

import ru.practicum.shareit.common.model.PaginationConfig;
import ru.practicum.shareit.request.model.dto.RequestItemsDto;
import ru.practicum.shareit.request.model.dto.RequestDescription;
import ru.practicum.shareit.request.model.dto.RequestDto;

import java.util.List;

public interface RequestService {
    RequestDto create(Long userId, RequestDescription requestDescription);

    List<RequestItemsDto> getOwn(Long userId);

    List<RequestItemsDto> getAll(Long userId, PaginationConfig paginationConfig);

    RequestItemsDto getById(Long userId, Long requestId);
}
